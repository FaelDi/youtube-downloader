package com.rafasterch.youtube.downloader.security;

import com.rafasterch.youtube.downloader.exceptions.InvalidUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UrlValidatorTest {

    private UrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator("youtube.com,youtu.be,www.youtube.com");
    }

    @Test
    void validate_withValidYoutubeUrl_doesNotThrow() {
        assertThatNoException().isThrownBy(
            () -> validator.validate("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        );
    }

    @Test
    void validate_withAllowedShortUrl_doesNotThrow() {
        assertThatNoException().isThrownBy(
            () -> validator.validate("https://youtu.be/dQw4w9WgXcQ")
        );
    }

    @Test
    void validate_withDisallowedDomain_throwsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("https://evil.com/video"))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("Domain not allowed");
    }

    @Test
    void validate_withFtpScheme_throwsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("ftp://youtube.com/video"))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("HTTP or HTTPS");
    }

    @Test
    void validate_withMalformedUrl_throwsInvalidUrlException() {
        assertThatThrownBy(() -> validator.validate("not a url :::"))
            .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void validate_withLoopbackAddress_throwsInvalidUrlException() {
        // localhost is not in the allowlist — should be rejected before IP check
        assertThatThrownBy(() -> validator.validate("http://localhost/admin"))
            .isInstanceOf(InvalidUrlException.class);
    }
}
