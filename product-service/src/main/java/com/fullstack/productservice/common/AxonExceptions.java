package com.fullstack.productservice.common;

import java.util.concurrent.ExecutionException;

/**
 * Axon wraps whatever a Command/Query handler throws inside
 * CommandExecutionException/QueryExecutionException, and CompletableFuture#get()
 * wraps that again in ExecutionException. Without unwrapping, business
 * exceptions (e.g. IllegalArgumentException from an Aggregate invariant check)
 * never reach commonservice's ExceptionAdvice as themselves, so they fall
 * through to the generic 500 handler instead of the intended 400.
 */
public final class AxonExceptions {

    private AxonExceptions() {
    }

    public static IllegalArgumentException unwrap(ExecutionException e) {
        Throwable cause = e.getCause();
        String message = cause != null ? cause.getMessage() : e.getMessage();
        return new IllegalArgumentException(message, e);
    }
}
