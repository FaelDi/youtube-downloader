import os


def _require(key: str) -> str:
    val = os.environ.get(key)
    if not val:
        raise ValueError(f"Required environment variable {key!r} is not set")
    return val


REDIS_HOST: str = os.environ.get("REDIS_HOST", "localhost")
REDIS_PORT: int = int(os.environ.get("REDIS_PORT", 6379))
REDIS_QUEUE: str = os.environ.get("REDIS_QUEUE", "download:jobs")

DB_HOST: str = os.environ.get("DB_HOST", "localhost")
DB_PORT: int = int(os.environ.get("DB_PORT", 5432))
DB_NAME: str = os.environ.get("DB_NAME", "mediadb")
DB_USER: str = _require("DB_USER")
DB_PASS: str = _require("DB_PASS")

MINIO_ENDPOINT: str = os.environ.get("MINIO_ENDPOINT", "http://localhost:9000")
MINIO_ACCESS_KEY: str = _require("MINIO_ACCESS_KEY")
MINIO_SECRET_KEY: str = _require("MINIO_SECRET_KEY")
MINIO_BUCKET: str = os.environ.get("MINIO_BUCKET", "downloads")
SIGNED_URL_EXPIRY_HOURS: int = int(os.environ.get("SIGNED_URL_EXPIRY_HOURS", 24))

TMP_DIR: str = os.environ.get("TMP_DIR", "/tmp/ytdl")
LOG_LEVEL: str = os.environ.get("LOG_LEVEL", "INFO")

DSN: str = f"postgresql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
