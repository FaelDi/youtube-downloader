package com.rafasterch.youtube.downloader.controller;

import com.rafasterch.youtube.downloader.dto.CreateJobRequest;
import com.rafasterch.youtube.downloader.dto.JobStatusResponse;
import com.rafasterch.youtube.downloader.service.DownloadJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Download Jobs", description = "Manage async media download jobs")
public class DownloadJobController {

    private final DownloadJobService jobService;

    @PostMapping
    @Operation(summary = "Create a download job", description = "Submits a media URL for async download. Returns 202 Accepted with the initial job state.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Job accepted and queued"),
        @ApiResponse(responseCode = "400", description = "Invalid URL or validation error"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<JobStatusResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobStatusResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job status", description = "Returns the current state of a download job, including signed URL when completed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job found"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobStatusResponse> getJob(@PathVariable UUID id) {
        JobStatusResponse response = jobService.getJob(id);
        return ResponseEntity.ok(response);
    }
}
