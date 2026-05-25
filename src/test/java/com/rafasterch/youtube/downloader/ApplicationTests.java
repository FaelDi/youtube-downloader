package com.rafasterch.youtube.downloader;

import com.rafasterch.youtube.downloader.enums.JobStatus;
import com.rafasterch.youtube.downloader.security.UrlValidator;
import com.rafasterch.youtube.downloader.exceptions.InvalidUrlException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ApplicationTests {

    @Test
    void allJobStatusValuesArePresent() {
        assertThat(JobStatus.values()).hasSize(9);
        assertThat(JobStatus.valueOf("PENDING")).isNotNull();
        assertThat(JobStatus.valueOf("QUEUED")).isNotNull();
        assertThat(JobStatus.valueOf("DOWNLOADING")).isNotNull();
        assertThat(JobStatus.valueOf("PROCESSING")).isNotNull();
        assertThat(JobStatus.valueOf("UPLOADING")).isNotNull();
        assertThat(JobStatus.valueOf("COMPLETED")).isNotNull();
        assertThat(JobStatus.valueOf("FAILED")).isNotNull();
        assertThat(JobStatus.valueOf("CANCELLED")).isNotNull();
        assertThat(JobStatus.valueOf("EXPIRED")).isNotNull();
    }

    @Test
    void urlValidator_rejectsDisallowedDomain() {
        UrlValidator validator = new UrlValidator("youtube.com,youtu.be,www.youtube.com");
        assertThatThrownBy(() -> validator.validate("https://evil.com/video"))
            .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void urlValidator_acceptsAllowedDomain() {
        UrlValidator validator = new UrlValidator("youtube.com,youtu.be,www.youtube.com");
        assertThatNoException().isThrownBy(
            () -> validator.validate("https://www.youtube.com/watch?v=test")
        );
    }
}
