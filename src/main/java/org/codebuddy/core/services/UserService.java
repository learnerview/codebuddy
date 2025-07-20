package org.codebuddy.core.services;

import org.codebuddy.core.dao.UserDao;
import org.codebuddy.core.models.User;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDao userDao = new UserDao();

    public void registerUser(String username, String password, String email) throws SQLException {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(0, username, passwordHash, email, LocalDateTime.now());
        userDao.saveUser(user);
    }

    public User authenticateUser(String username, String password) throws SQLException {
        System.out.println("[UserService] authenticateUser called for username: " + username);
        User user = userDao.findByUsername(username);
        System.out.println("[UserService] user from DB: " + user);
        if (user != null) {
            boolean passwordMatch = org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPasswordHash());
            System.out.println("[UserService] password match: " + passwordMatch);
            if (passwordMatch) {
                return user;
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        return userDao.findByUsername(username) != null;
    }

    public boolean emailExists(String email) throws SQLException {
        return userDao.findByEmail(email) != null;
    }
} 