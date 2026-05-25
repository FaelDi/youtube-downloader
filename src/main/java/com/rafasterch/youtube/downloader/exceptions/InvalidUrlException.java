package com.rafasterch.youtube.downloader.exceptions;

public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
