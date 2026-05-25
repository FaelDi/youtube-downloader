#!/usr/bin/env python3
"""YouTube downloader worker — consumes Redis jobs and processes media downloads."""

import json
import logging
import os
import shutil
import sys
import time

import redis
from dotenv import load_dotenv

load_dotenv()

from config import LOG_LEVEL, REDIS_HOST, REDIS_PORT, REDIS_QUEUE, TMP_DIR
import db
import downloader
import uploader

logging.basicConfig(
    level=getattr(logging, LOG_LEVEL, logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    stream=sys.stdout,
)
log = logging.getLogger("worker")


def process_job(job_id: str) -> None:
    job = db.get_job(job_id)
    if job is None:
        log.warning("[JOB NOT FOUND] %s", job_id)
        return

    try:
        url = job["url"]
        fmt = job["format"] or "mp4"

        db.update_status(job_id, "DOWNLOADING")
        try:
            file_path = downloader.download(job_id, url, fmt)
        except Exception as e:
            db.mark_failed(job_id, str(e))
            return

        db.update_status(job_id, "UPLOADING")
        try:
            signed_url, expires_at = uploader.upload(job_id, file_path)
        except Exception as e:
            db.mark_failed(job_id, str(e))
            return

        file_name = os.path.basename(file_path)
        db.update_status(
            job_id,
            "COMPLETED",
            signed_url=signed_url,
            signed_url_expires_at=expires_at.isoformat(),
            file_name=file_name,
        )
        log.info("[JOB COMPLETED] Job %s", job_id)

    finally:
        shutil.rmtree(os.path.join(TMP_DIR, job_id), ignore_errors=True)


def main() -> None:
    log.info(
        "[WORKER STARTED] Connecting to Redis %s:%s, queue=%s",
        REDIS_HOST,
        REDIS_PORT,
        REDIS_QUEUE,
    )
    r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
    os.makedirs(TMP_DIR, exist_ok=True)

    while True:
        try:
            result = r.blpop(REDIS_QUEUE, timeout=5)
            if result is None:
                continue
            _, payload = result
            try:
                data = json.loads(payload)
            except json.JSONDecodeError:
                log.error("[DEAD LETTER] Invalid JSON payload, routing to dead-letter queue: %s", payload)
                r.rpush("dead-letter:download-jobs", payload)
                continue
            job_id = data.get("jobId")
            if not job_id:
                log.warning("[INVALID PAYLOAD] Missing jobId: %s", payload)
                continue
            log.info("[JOB RECEIVED] Processing job %s", job_id)
            process_job(job_id)
        except redis.ConnectionError as e:
            log.error("[REDIS ERROR] Connection failed: %s", e)
            time.sleep(5)
        except Exception as e:
            log.error("[WORKER ERROR] Unexpected error: %s", e, exc_info=True)


if __name__ == "__main__":
    main()
