package com.mtech.adms.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Wraps BCrypt hashing so the rest of the app never touches the
 * hashing library directly. If we ever switch algorithms, only
 * this class changes.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password for storage. Never store the
     * plain-text password itself, only the result of this method.
     */
    public static String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain-text password against a previously stored hash.
     */
    public static boolean verify(String plainTextPassword, String storedHash) {
        return BCrypt.checkpw(plainTextPassword, storedHash);
    }
}