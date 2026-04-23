package com.example.bookmark_saver.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * 
 * Intercepts application-wide exceptions and maps them to structured error responses
 * following the RFC 9457 {@link ProblemDetail} format.
 */
@RestControllerAdvice
public class ErrorHandler {
        /**
         * Handles all unhandled exceptions with HTTP 500.
         *
         * @param exception The caught exception.
         * @param request   The current HTTP request.
         * 
         * @return A {@link ProblemDetail} with status 500.
         */
        @ExceptionHandler(Exception.class)
        public ProblemDetail handleGeneric(
                Exception exception,
                HttpServletRequest request
        ) {
                return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
        }

        /**
         * Handles not-found exceptions with HTTP 404.
         *
         * @param exception The caught exception.
         * @param request   The current HTTP request.
         * 
         * @return A {@link ProblemDetail} with status 404.
         */
        @ExceptionHandler({
                NoHandlerFoundException.class,
                EntityNotFoundException.class
        })
        public ProblemDetail handleNotFound(
                Exception exception,
                HttpServletRequest request
        ) {
                return createProblemDetail(HttpStatus.NOT_FOUND, exception, request);
        }

        /**
         * Handles illegal argument exceptions with HTTP 409.
         *
         * @param exception The caught exception.
         * @param request   The current HTTP request.
         * 
         * @return A {@link ProblemDetail} with status 409.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ProblemDetail handleConflict(
                IllegalArgumentException exception,
                HttpServletRequest request
        ) {
                return createProblemDetail(HttpStatus.CONFLICT, exception, request);
        }

        /**
         * Handles unsupported HTTP method exceptions with HTTP 405.
         *
         * @param exception The caught exception.
         * @param request   The current HTTP request.
         * 
         * @return A {@link ProblemDetail} with status 405.
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ProblemDetail handleMethodNotAllowed(
                HttpRequestMethodNotSupportedException exception,
                HttpServletRequest request
        ) {
                return createProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, exception, request);
        }

        /**
         * Handles validation failures with HTTP 400.
         * Aggregates all field errors into a single detail string.
         *
         * @param exception The caught validation exception.
         * @param request   The current HTTP request.
         * 
         * @return A {@link ProblemDetail} with status 400 and field-level error details.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleValidation(
                MethodArgumentNotValidException exception,
                HttpServletRequest request
        ) {
                ProblemDetail problem = createProblemDetail(HttpStatus.BAD_REQUEST, exception, request);

                String detail = exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                problem.setDetail(detail);

                return problem;
        }

        /**
         * Builds a {@link ProblemDetail} response.
         */
        private ProblemDetail createProblemDetail(
                HttpStatus status,
                Exception exception,
                HttpServletRequest request
        ) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

                problem.setTitle(status.getReasonPhrase());
                problem.setProperty("error", exception.getClass().getSimpleName());
                problem.setInstance(java.net.URI.create(request.getRequestURI()));
                problem.setProperty("timestamp", Instant.now());

                return problem;
        }
}