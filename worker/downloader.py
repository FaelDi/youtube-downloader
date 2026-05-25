import logging
import os

import yt_dlp

from config import TMP_DIR

log = logging.getLogger(__name__)


def download(job_id: str, url: str, fmt: str) -> str:
    output_dir = os.path.join(TMP_DIR, job_id)
    os.makedirs(output_dir, exist_ok=True)

    opts = {
        "outtmpl": os.path.join(output_dir, "%(title)s.%(ext)s"),
        "noplaylist": True,
        "quiet": False,
        "no_warnings": False,
    }

    if fmt in ("mp4", "webm"):
        opts["format"] = "bestvideo+bestaudio/best"
    else:
        opts["format"] = "bestaudio/best"

    if fmt == "mp3":
        opts["postprocessors"] = [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
            }
        ]

    log.info("[DOWNLOAD STARTED] Job %s url=%s format=%s", job_id, url, fmt)

    try:
        with yt_dlp.YoutubeDL(opts) as ydl:
            ydl.download([url])

        files = os.listdir(output_dir)
        if not files:
            raise FileNotFoundError(f"No output file found in {output_dir} after download")

        file_path = os.path.join(output_dir, files[0])
        log.info("[DOWNLOAD COMPLETE] Job %s file=%s", job_id, file_path)
        return file_path

    except Exception as e:
        log.error("[DOWNLOAD ERROR] Job %s: %s", job_id, e)
        raise
