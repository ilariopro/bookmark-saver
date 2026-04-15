package com.example.bookmark_saver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Bookmark Saver application.
 *
 * Enables asynchronous execution to support background tasks.
 */
@EnableAsync
@EnableRetry
@SpringBootApplication
public class BookmarkSaverApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookmarkSaverApplication.class, args);
    }
}