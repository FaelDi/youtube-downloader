import logging
import os
from datetime import datetime, timedelta, timezone
from urllib.parse import urlparse

from minio import Minio
from minio.error import S3Error

from config import (
    MINIO_ACCESS_KEY,
    MINIO_BUCKET,
    MINIO_ENDPOINT,
    MINIO_SECRET_KEY,
    SIGNED_URL_EXPIRY_HOURS,
)

log = logging.getLogger(__name__)


def get_client() -> Minio:
    parsed = urlparse(MINIO_ENDPOINT)
    secure = parsed.scheme == "https"
    host = parsed.netloc
    return Minio(host, access_key=MINIO_ACCESS_KEY, secret_key=MINIO_SECRET_KEY, secure=secure)


def ensure_bucket(client: Minio) -> None:
    if not client.bucket_exists(MINIO_BUCKET):
        client.make_bucket(MINIO_BUCKET)
        log.info("[BUCKET CREATED] %s", MINIO_BUCKET)


def upload(job_id: str, file_path: str) -> tuple[str, datetime]:
    object_name = f"jobs/{job_id}/{os.path.basename(file_path)}"

    client = get_client()
    ensure_bucket(client)

    try:
        client.fput_object(MINIO_BUCKET, object_name, file_path)
        log.info("[UPLOAD COMPLETE] Job %s -> %s", job_id, object_name)

        url = client.presigned_get_object(
            MINIO_BUCKET,
            object_name,
            expires=timedelta(hours=SIGNED_URL_EXPIRY_HOURS),
        )
        expires_at = datetime.now(timezone.utc) + timedelta(hours=SIGNED_URL_EXPIRY_HOURS)

        return url, expires_at

    except S3Error as e:
        log.error("[UPLOAD ERROR] Job %s: %s", job_id, e)
        raise
