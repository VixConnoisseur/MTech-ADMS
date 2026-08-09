package com.mtech.adms.service;

import com.mtech.adms.dao.UserDao;
import com.mtech.adms.exception.AuthenticationException;
import com.mtech.adms.model.User;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.PasswordUtil;

import java.util.Optional;

/**
 * Business logic for authentication. Controllers call this instead
 * of touching UserDao or PasswordUtil directly.
 */
public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    /**
     * Attempts to authenticate a user with the given credentials.
     *
     * @throws AuthenticationException if the username doesn't exist,
     *         the password is wrong, or the account is inactive.
     *         The message is intentionally generic in all three cases.
     */
    public User login(String username, String plainTextPassword) {
        if (username == null || username.isBlank() ||
                plainTextPassword == null || plainTextPassword.isBlank()) {
            throw new AuthenticationException("Username and password are required.");
        }

        Optional<User> userOpt = userDao.findByUsername(username.trim());

        if (userOpt.isEmpty()) {
            AppLogger.warn("Login attempt for unknown username: " + username);
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            AppLogger.warn("Login attempt for inactive account: " + username);
            throw new AuthenticationException("This account has been deactivated.");
        }

        if (!PasswordUtil.verify(plainTextPassword, user.getPasswordHash())) {
            AppLogger.warn("Failed login attempt (wrong password) for: " + username);
            throw new AuthenticationException("Invalid username or password.");
        }

        userDao.updateLastLogin(user.getId());
        AppLogger.info("User logged in: " + username);

        return user;
    }
}