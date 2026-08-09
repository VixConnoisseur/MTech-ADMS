package com.mtech.adms.util;

import com.mtech.adms.model.User;

/**
 * Holds the currently logged-in user for the lifetime of the
 * running application. Simple singleton - appropriate here since
 * a desktop JavaFX app has exactly one active session at a time,
 * unlike a multi-user web server.
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}