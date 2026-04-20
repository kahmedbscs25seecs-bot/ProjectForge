package com.forge.service;

import com.forge.model.User;
import com.forge.repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public int register(String username, String email, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        User user = new User(username, email, hashedPassword);
        return userRepository.create(user);
    }

    public Optional<User> login(String username, String password) throws SQLException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPasswordHash().equals(hashPassword(password))) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(int id) throws SQLException {
        return userRepository.findById(id);
    }

    public void updateUser(User user) throws SQLException {
        userRepository.update(user);
    }

    public void addCoins(int userId, int amount) throws SQLException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setCoins(user.getCoins() + amount);
            userRepository.update(user);
        }
    }

    public void subtractCoins(int userId, int amount) throws SQLException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int newCoins = user.getCoins() - amount;
            user.setCoins(Math.max(0, newCoins));
            userRepository.update(user);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}