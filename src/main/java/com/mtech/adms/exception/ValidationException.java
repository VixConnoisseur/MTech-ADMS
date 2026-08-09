package com.mtech.adms.exception;

/**
 * Thrown by the Service layer when input fails a business rule
 * (e.g. duplicate Asset ID, invalid status transition, missing
 * required field). Controllers catch this specifically to show
 * a user-friendly validation message, as opposed to a generic
 * "something went wrong" error.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}