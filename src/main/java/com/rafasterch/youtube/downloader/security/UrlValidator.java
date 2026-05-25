package com.rafasterch.youtube.downloader.security;

import com.rafasterch.youtube.downloader.exceptions.InvalidUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UrlValidator {

    private static final Logger log = LoggerFactory.getLogger(UrlValidator.class);

    private final Set<String> allowedDomains;

    public UrlValidator(@Value("${app.allowed-domains}") String allowedDomainsConfig) {
        this.allowedDomains = Arrays.stream(allowedDomainsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public void validate(String rawUrl) {
        URI uri;
        try {
            uri = new URI(rawUrl);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new InvalidUrlException("URL must use HTTP or HTTPS scheme");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("URL has no host");
        }

        if (!allowedDomains.contains(host)) {
            log.debug("[URL REJECTED] Domain not in allowlist: {}", host);
            throw new InvalidUrlException("URL is not from an allowed source");
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new InvalidUrlException("Cannot resolve host");
        }

        if (address.isSiteLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isMulticastAddress()) {
            throw new InvalidUrlException("URL resolves to a private or reserved address");
        }

        log.debug("[URL VALIDATED] URL passed all security checks: {}", host);
    }
}
