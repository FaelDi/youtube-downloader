package com.rafasterch.youtube.downloader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafasterch.youtube.downloader.dto.CreateJobRequest;
import com.rafasterch.youtube.downloader.dto.JobStatusResponse;
import com.rafasterch.youtube.downloader.exceptions.InvalidUrlException;
import com.rafasterch.youtube.downloader.exceptions.ResourceNotFoundException;
import com.rafasterch.youtube.downloader.service.DownloadJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DownloadJobController.class)
class DownloadJobControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DownloadJobService jobService;

    @Test
    void createJob_withValidRequest_returns202() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setUrl("https://www.youtube.com/watch?v=test");
        request.setFormat("mp4");

        JobStatusResponse response = buildResponse("QUEUED");
        when(jobService.createJob(any())).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void createJob_withBlankUrl_returns400() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setUrl("");
        request.setFormat("mp4");

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_whenUrlNotAllowed_returns400() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setUrl("https://evil.com/video");
        request.setFormat("mp4");

        when(jobService.createJob(any()))
            .thenThrow(new InvalidUrlException("Domain not allowed: evil.com"));

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_URL"));
    }

    @Test
    void getJob_withExistingId_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(jobService.getJob(id)).thenReturn(buildResponse("COMPLETED"));

        mockMvc.perform(get("/api/jobs/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getJob_withUnknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(jobService.getJob(id)).thenThrow(new ResourceNotFoundException("Job not found: " + id));

        mockMvc.perform(get("/api/jobs/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private JobStatusResponse buildResponse(String status) {
        return new JobStatusResponse(
            UUID.randomUUID(),
            "https://www.youtube.com/watch?v=test",
            "mp4",
            status,
            null,
            null,
            null,
            null,
            Instant.now(),
            Instant.now()
        );
    }
}
