package com.rafasterch.youtube.downloader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJobRequest {

    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    @Pattern(regexp = "^(mp4|mp3|webm)$", message = "Format must be mp4, mp3, or webm")
    private String format = "mp4";
}
