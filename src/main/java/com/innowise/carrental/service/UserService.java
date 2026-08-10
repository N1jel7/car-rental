package com.innowise.carrental.service;

import com.innowise.carrental.dao.UserDao;
import com.innowise.carrental.dao.impl.UserDaoImpl;
import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;

    // We accept UserDao as a constructor parameter (dependency injection by hand,
    // without any framework). This makes it easy to substitute a mock in tests:
    // new UserService(mockUserDao) — no real DB needed in unit tests.
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Default constructor for production use — wires the real implementation.
    public UserService() {
        this(new UserDaoImpl());
    }

    public User register(String email, String password, String fullName, String phone)
            throws ServiceException, ValidationException {
        validateEmail(email);
        validatePassword(password);
        validateFullName(fullName);

        try {
            if (userDao.findByEmail(email).isPresent()) {
                throw new ValidationException("Email already registered: " + email);
            }

            User user = new User();
            user.setEmail(email.toLowerCase().strip());
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setFullName(fullName.strip());
            user.setPhone(phone != null ? phone.strip() : null);
            user.setRole(Role.USER);
            user.setLocale("ru");

            userDao.save(user);
            log.info("Registered new user email={}", email);
            return user;

        } catch (DaoException e) {
            log.error("Failed to register user email={}", email, e);
            throw new ServiceException("Registration failed", e);
        }
    }

    public User login(String email, String password)
            throws ServiceException, ValidationException {
        validateEmail(email);

        if (password == null || password.isBlank()) {
            throw new ValidationException("Password must not be empty");
        }

        try {
            Optional<User> found = userDao.findByEmail(email.toLowerCase().strip());

            if (found.isEmpty() || !PasswordUtil.verify(password, found.get().getPasswordHash())) {
                throw new ValidationException("Invalid email or password");
            }

            log.info("User logged in email={}", email);
            return found.get();

        } catch (DaoException e) {
            log.error("Login failed for email={}", email, e);
            throw new ServiceException("Login failed", e);
        }
    }

    public User findById(long id) throws ServiceException {
        try {
            return userDao.findById(id)
                    .orElseThrow(() -> new ServiceException("User not found: " + id));
        } catch (DaoException e) {
            log.error("Failed to find user id={}", id, e);
            throw new ServiceException("Failed to find user", e);
        }
    }

    public void updateProfile(long userId, String fullName, String phone, String locale)
            throws ServiceException, ValidationException {
        validateFullName(fullName);

        try {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new ServiceException("User not found: " + userId));

            user.setFullName(fullName.strip());
            user.setPhone(phone != null ? phone.strip() : null);
            user.setLocale(locale);

            userDao.update(user);
            log.info("Updated profile for userId={}", userId);

        } catch (DaoException e) {
            log.error("Failed to update profile userId={}", userId, e);
            throw new ServiceException("Failed to update profile", e);
        }
    }

    public void changePassword(long userId, String oldPassword, String newPassword)
            throws ServiceException, ValidationException {
        validatePassword(newPassword);

        try {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new ServiceException("User not found: " + userId));

            if (!PasswordUtil.verify(oldPassword, user.getPasswordHash())) {
                throw new ValidationException("Current password is incorrect");
            }

            user.setPasswordHash(PasswordUtil.hash(newPassword));
            userDao.update(user);
            log.info("Password changed for userId={}", userId);

        } catch (DaoException e) {
            log.error("Failed to change password userId={}", userId, e);
            throw new ServiceException("Failed to change password", e);
        }
    }

    public List<User> findAll(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return userDao.findAll(offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to find all users page={}", page, e);
            throw new ServiceException("Failed to load users", e);
        }
    }

    public int countAll() throws ServiceException {
        try {
            return userDao.countAll();
        } catch (DaoException e) {
            log.error("Failed to count users", e);
            throw new ServiceException("Failed to count users", e);
        }
    }


    private void validateEmail(String email) throws ValidationException {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email must not be empty");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new ValidationException("Invalid email format");
        }
    }

    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }

    private void validateFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Full name must not be empty");
        }
    }

}
