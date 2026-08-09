package com.mtech.adms.exception;

/**
 * Thrown when login credentials are invalid, or the account is inactive.
 * Kept generic on purpose - the message shown to the user should never
 * reveal whether the username or the password was the incorrect part,
 * as that would help an attacker enumerate valid usernames.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}