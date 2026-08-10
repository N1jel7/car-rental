package com.innowise.carrental.dao.impl;

import com.innowise.carrental.dao.UserDao;
import com.innowise.carrental.db.ConnectionPool;
import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT id, email, password_hash, full_name, phone,
                   avatar_path, role, locale, created_at
            FROM users
            WHERE id = ?
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT id, email, password_hash, full_name, phone,
                   avatar_path, role, locale, created_at
            FROM users
            WHERE email = ?
            """;

    private static final String FIND_ALL_PAGED = """
            SELECT id, email, password_hash, full_name, phone,
                   avatar_path, role, locale, created_at
            FROM users
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_ALL = "SELECT COUNT(*) FROM users";

    private static final String SAVE = """
            INSERT INTO users (email, password_hash, full_name, phone,
                               avatar_path, role, locale)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE users
            SET email = ?, full_name = ?, phone = ?, avatar_path = ?, locale = ?
            WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM users WHERE id = ?";


    @Override
    public Optional<User> findById(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            log.error("Failed to find user by id={}", id, e);
            throw new DaoException("Failed to find user by id: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            log.error("Failed to find user by email={}", email, e);
            throw new DaoException("Failed to find user by email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() throws DaoException {
        throw new UnsupportedOperationException("Use findAll(offset, limit)");
    }

    @Override
    public List<User> findAll(int offset, int limit) throws DaoException {
        List<User> users = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_PAGED)) {

            statement.setInt(1, limit);
            statement.setInt(2, offset);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find all users offset={} limit={}", offset, limit, e);
            throw new DaoException("Failed to find all users", e);
        }

        return users;
    }

    @Override
    public int countAll() throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_ALL);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            log.error("Failed to count users", e);
            throw new DaoException("Failed to count users", e);
        }
    }

    @Override
    public void save(User user) throws DaoException {

        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getAvatarPath());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user.getLocale());

            statement.executeUpdate();


            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to save user email={}", user.getEmail(), e);
            throw new DaoException("Failed to save user: " + user.getEmail(), e);
        }
    }

    @Override
    public void update(User user) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getAvatarPath());
            statement.setString(5, user.getLocale());
            statement.setLong(6, user.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update user id={}", user.getId(), e);
            throw new DaoException("Failed to update user: " + user.getId(), e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete user id={}", id, e);
            throw new DaoException("Failed to delete user: " + id, e);
        }
    }

    // Maps the current row of a ResultSet to a User object
    // Cursor must already be positioned on a valid row
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setLocale(rs.getString("locale"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }

}
