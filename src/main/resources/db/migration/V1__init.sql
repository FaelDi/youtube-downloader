-- Create schema
CREATE SCHEMA IF NOT EXISTS media_downloader;

-- Create download_jobs table
CREATE TABLE media_downloader.download_jobs (
    id                   UUID                     NOT NULL DEFAULT gen_random_uuid(),
    url                  TEXT                     NOT NULL,
    format               VARCHAR(20)              NOT NULL DEFAULT 'mp4',
    status               VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    file_name            VARCHAR(500),
    signed_url           TEXT,
    signed_url_expires_at TIMESTAMPTZ,
    error_message        TEXT,
    created_at           TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ              NOT NULL DEFAULT now(),

    CONSTRAINT pk_download_jobs PRIMARY KEY (id),
    CONSTRAINT chk_download_jobs_format CHECK (format IN ('mp4', 'mp3', 'webm')),
    CONSTRAINT chk_download_jobs_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Indexes
CREATE INDEX idx_download_jobs_status     ON media_downloader.download_jobs (status);
CREATE INDEX idx_download_jobs_created_at ON media_downloader.download_jobs (created_at DESC);
