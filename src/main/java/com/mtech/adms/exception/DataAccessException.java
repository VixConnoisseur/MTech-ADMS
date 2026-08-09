package com.mtech.adms.exception;

/**
 * Thrown when a database operation fails at the DAO layer
 * (connection issues, SQL errors, constraint violations, etc.).
 * Unchecked, because callers up the stack generally can't recover
 * from a database failure mid-operation - it should surface as an
 * error the user is informed about, not silently swallowed.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}