package com.shanwinx.judge_service.exception;

/**
 * Signals a transient infrastructure error that should be retried via backoff queues.
 */
public class RetryableJudgeException extends RuntimeException {

    public RetryableJudgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
