package com.rafasterch.youtube.downloader.repository;

import com.rafasterch.youtube.downloader.entity.DownloadJob;
import com.rafasterch.youtube.downloader.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DownloadJobRepository extends JpaRepository<DownloadJob, UUID> {

    List<DownloadJob> findByStatus(JobStatus status);

    Page<DownloadJob> findByStatus(JobStatus status, Pageable pageable);
}
