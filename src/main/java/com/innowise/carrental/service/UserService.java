package com.innowise.carrental.service;

import com.innowise.carrental.dao.UserDao;
import com.innowise.carrental.dao.impl.UserDaoImpl;
import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.util.PasswordUtil;
import com.innowise.carrental.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserService() {
        this(new UserDaoImpl());
    }

    public User register(String email, String password, String fullName, String phone)
            throws ServiceException, ValidationException {

        ValidatorUtil.validateRegistration(email, password, fullName);

        try {
            if (userDao.findByEmail(email).isPresent()) {
                throw new ValidationException("Email already registered: " + email);
            }

            User user = User.builder()
                    .email(email.toLowerCase().strip())
                    .passwordHash(PasswordUtil.hash(password))
                    .fullName(fullName.strip())
                    .phone(phone != null ? phone.strip() : null)
                    .role(Role.USER)
                    .locale("ru")
                    .build();

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
        ValidatorUtil.validateLogin(email, password);

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
        ValidatorUtil.notBlank(fullName, "Full name must not be empty");

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
        ValidatorUtil.minLength(newPassword, 8, "Password must be at least 8 characters");

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

}
