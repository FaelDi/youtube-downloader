-- Drop the incomplete constraint and replace with all 9 valid JobStatus values
ALTER TABLE media_downloader.download_jobs
    DROP CONSTRAINT IF EXISTS chk_download_jobs_status;

ALTER TABLE media_downloader.download_jobs
    ADD CONSTRAINT chk_download_jobs_status CHECK (
        status IN (
            'PENDING', 'QUEUED', 'DOWNLOADING',
            'PROCESSING', 'UPLOADING', 'COMPLETED',
            'FAILED', 'CANCELLED', 'EXPIRED'
        )
    );
