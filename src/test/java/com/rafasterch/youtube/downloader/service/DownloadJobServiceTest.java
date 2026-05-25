package com.rafasterch.youtube.downloader.service;

import com.rafasterch.youtube.downloader.dto.CreateJobRequest;
import com.rafasterch.youtube.downloader.dto.JobStatusResponse;
import com.rafasterch.youtube.downloader.entity.DownloadJob;
import com.rafasterch.youtube.downloader.enums.JobStatus;
import com.rafasterch.youtube.downloader.exceptions.InvalidUrlException;
import com.rafasterch.youtube.downloader.exceptions.ResourceNotFoundException;
import com.rafasterch.youtube.downloader.repository.DownloadJobRepository;
import com.rafasterch.youtube.downloader.security.UrlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadJobServiceTest {

    @Mock
    DownloadJobRepository repository;

    @Mock
    RedisPublisherService redisPublisher;

    @Mock
    UrlValidator urlValidator;

    @InjectMocks
    DownloadJobService service;

    @Test
    void createJob_withValidRequest_returnsJobStatusResponse() {
        // arrange
        CreateJobRequest request = new CreateJobRequest();
        request.setUrl("https://www.youtube.com/watch?v=test");
        request.setFormat("mp4");

        DownloadJob savedJob = buildJob(JobStatus.QUEUED);
        when(repository.save(any())).thenReturn(savedJob);

        // act
        JobStatusResponse response = service.createJob(request);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("QUEUED");
        verify(urlValidator).validate(request.getUrl());
        verify(redisPublisher).publish(savedJob.getId());
    }

    @Test
    void createJob_whenUrlValidatorThrows_propagatesException() {
        CreateJobRequest request = new CreateJobRequest();
        request.setUrl("https://evil.com");
        request.setFormat("mp4");

        doThrow(new InvalidUrlException("Domain not allowed: evil.com"))
            .when(urlValidator).validate(any());

        assertThatThrownBy(() -> service.createJob(request))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("Domain not allowed");

        verify(repository, never()).save(any());
        verify(redisPublisher, never()).publish(any());
    }

    @Test
    void getJob_withExistingId_returnsResponse() {
        UUID id = UUID.randomUUID();
        DownloadJob job = buildJob(JobStatus.COMPLETED);
        when(repository.findById(id)).thenReturn(Optional.of(job));

        JobStatusResponse response = service.getJob(id);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void getJob_withUnknownId_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getJob(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    private DownloadJob buildJob(JobStatus status) {
        DownloadJob job = new DownloadJob();
        job.setId(UUID.randomUUID());
        job.setUrl("https://www.youtube.com/watch?v=test");
        job.setFormat("mp4");
        job.setStatus(status);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        return job;
    }
}
