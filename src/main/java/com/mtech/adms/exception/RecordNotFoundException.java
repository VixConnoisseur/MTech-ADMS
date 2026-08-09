package com.mtech.adms.exception;

/**
 * Thrown when a lookup by ID or unique key finds no matching record.
 * Kept separate from DataAccessException because "not found" is an
 * expected, recoverable case (e.g. show "Asset not found" to the
 * user) rather than a genuine database failure.
 */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(String message) {
        super(message);
    }
}