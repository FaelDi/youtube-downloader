package com.rafasterch.youtube.downloader.service;

import com.rafasterch.youtube.downloader.dto.CreateJobRequest;
import com.rafasterch.youtube.downloader.dto.JobStatusResponse;
import com.rafasterch.youtube.downloader.entity.DownloadJob;
import com.rafasterch.youtube.downloader.enums.JobStatus;
import com.rafasterch.youtube.downloader.exceptions.ResourceNotFoundException;
import com.rafasterch.youtube.downloader.repository.DownloadJobRepository;
import com.rafasterch.youtube.downloader.security.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DownloadJobService {

    private static final Logger log = LoggerFactory.getLogger(DownloadJobService.class);

    private final DownloadJobRepository repository;
    private final RedisPublisherService redisPublisher;
    private final UrlValidator urlValidator;

    @Transactional
    public JobStatusResponse createJob(CreateJobRequest request) {
        urlValidator.validate(request.getUrl());

        DownloadJob job = new DownloadJob();
        job.setUrl(request.getUrl());
        job.setFormat(request.getFormat() != null ? request.getFormat() : "mp4");
        job.setStatus(JobStatus.QUEUED);
        job = repository.save(job);

        log.info("[JOB CREATED] Job {} queued for URL: {}", job.getId(), job.getUrl());

        final UUID jobId = job.getId();
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisPublisher.publish(jobId);
                }
            }
        );

        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public JobStatusResponse getJob(UUID id) {
        DownloadJob job = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        return toResponse(job);
    }

    private JobStatusResponse toResponse(DownloadJob job) {
        if (job.getSignedUrlExpiresAt() != null && Instant.now().isAfter(job.getSignedUrlExpiresAt())) {
            log.info("[SIGNED URL EXPIRED] Job {}", job.getId());
            return new JobStatusResponse(
                    job.getId(),
                    job.getUrl(),
                    job.getFormat(),
                    "EXPIRED",
                    job.getFileName(),
                    null,
                    job.getSignedUrlExpiresAt(),
                    job.getErrorMessage(),
                    job.getCreatedAt(),
                    job.getUpdatedAt()
            );
        }
        return new JobStatusResponse(
                job.getId(),
                job.getUrl(),
                job.getFormat(),
                job.getStatus().name(),
                job.getFileName(),
                job.getSignedUrl(),
                job.getSignedUrlExpiresAt(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
