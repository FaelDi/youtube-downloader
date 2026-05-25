import logging

import psycopg2
import psycopg2.pool
from psycopg2.extras import DictCursor

from config import DSN

log = logging.getLogger(__name__)

_pool: psycopg2.pool.SimpleConnectionPool | None = None


def _get_pool() -> psycopg2.pool.SimpleConnectionPool:
    global _pool
    if _pool is None:
        _pool = psycopg2.pool.SimpleConnectionPool(1, 5, DSN)
    return _pool


def get_job(job_id: str) -> dict | None:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn.cursor(cursor_factory=DictCursor) as cur:
            cur.execute(
                "SELECT * FROM media_downloader.download_jobs WHERE id = %s",
                (job_id,),
            )
            row = cur.fetchone()
            if row is None:
                return None
            return dict(row)
    finally:
        pool.putconn(conn)


def update_status(job_id: str, status: str, **kwargs: str) -> None:
    allowed_fields = {"file_name", "signed_url", "signed_url_expires_at", "error_message"}
    extra = {k: v for k, v in kwargs.items() if k in allowed_fields}

    set_clauses = ["status = %s", "updated_at = NOW()"]
    values = [status]

    for field, value in extra.items():
        set_clauses.append(f"{field} = %s")
        values.append(value)

    values.append(job_id)
    sql = f"UPDATE media_downloader.download_jobs SET {', '.join(set_clauses)} WHERE id = %s"

    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn.cursor() as cur:
            cur.execute(sql, values)
        conn.commit()
    finally:
        pool.putconn(conn)

    log.info("[STATUS UPDATE] Job %s -> %s", job_id, status)


def mark_failed(job_id: str, error: str) -> None:
    log.error("[JOB FAILED] Job %s: %s", job_id, error)
    update_status(job_id, "FAILED", error_message=error[:1000])
