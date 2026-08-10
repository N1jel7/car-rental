package com.innowise.carrental.dao.impl;

import com.innowise.carrental.dao.ReviewDao;
import com.innowise.carrental.db.ConnectionPool;
import com.innowise.carrental.entity.Review;
import com.innowise.carrental.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDaoImpl implements ReviewDao {

    private static final Logger log = LoggerFactory.getLogger(ReviewDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT id, user_id, car_id, booking_id, rating, comment, created_at
            FROM reviews
            WHERE id = ?
            """;

    private static final String FIND_BY_CAR_ID_PAGED = """
            SELECT id, user_id, car_id, booking_id, rating, comment, created_at
            FROM reviews
            WHERE car_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_CAR_ID = """
            SELECT COUNT(*) FROM reviews WHERE car_id = ?
            """;

    private static final String FIND_BY_BOOKING_ID = """
            SELECT id, user_id, car_id, booking_id, rating, comment, created_at
            FROM reviews
            WHERE booking_id = ?
            """;

    private static final String AVERAGE_RATING_BY_CAR_ID = """
            SELECT COALESCE(AVG(rating), 0.0) FROM reviews WHERE car_id = ?
            """;

    private static final String SAVE = """
            INSERT INTO reviews (user_id, car_id, booking_id, rating, comment)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String DELETE = "DELETE FROM reviews WHERE id = ?";

    @Override
    public Optional<Review> findById(long id) throws DaoException {
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
            log.error("Failed to find review by id={}", id, e);
            throw new DaoException("Failed to find review by id: " + id, e);
        }
    }

    @Override
    public List<Review> findByCarId(long carId, int offset, int limit) throws DaoException {
        List<Review> reviews = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CAR_ID_PAGED)) {

            statement.setLong(1, carId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find reviews for carId={}", carId, e);
            throw new DaoException("Failed to find reviews for car: " + carId, e);
        }

        return reviews;
    }

    @Override
    public int countByCarId(long carId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_CAR_ID)) {

            statement.setLong(1, carId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            log.error("Failed to count reviews for carId={}", carId, e);
            throw new DaoException("Failed to count reviews for car: " + carId, e);
        }
    }

    @Override
    public Optional<Review> findByBookingId(long bookingId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_BOOKING_ID)) {

            statement.setLong(1, bookingId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            log.error("Failed to find review by bookingId={}", bookingId, e);
            throw new DaoException("Failed to find review by bookingId: " + bookingId, e);
        }
    }

    @Override
    public double findAverageRatingByCarId(long carId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(AVERAGE_RATING_BY_CAR_ID)) {

            statement.setLong(1, carId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
                return 0.0;
            }

        } catch (SQLException e) {
            log.error("Failed to get average rating for carId={}", carId, e);
            throw new DaoException("Failed to get average rating for car: " + carId, e);
        }
    }

    @Override
    public void save(Review review) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, review.getUserId());
            statement.setLong(2, review.getCarId());
            statement.setLong(3, review.getBookingId());
            statement.setInt(4, review.getRating());
            statement.setString(5, review.getComment());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to save review bookingId={}", review.getBookingId(), e);
            throw new DaoException("Failed to save review", e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete review id={}", id, e);
            throw new DaoException("Failed to delete review: " + id, e);
        }
    }

    @Override
    public void update(Review review) throws DaoException {
        throw new UnsupportedOperationException("Reviews cannot be edited after submission");
    }

    @Override
    public List<Review> findAll() throws DaoException {
        throw new UnsupportedOperationException("Use findByCarId(carId, offset, limit)");
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setUserId(rs.getLong("user_id"));
        review.setCarId(rs.getLong("car_id"));
        review.setBookingId(rs.getLong("booking_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return review;
    }

}
