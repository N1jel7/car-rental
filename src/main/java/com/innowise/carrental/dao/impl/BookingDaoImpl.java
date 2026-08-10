package com.innowise.carrental.dao.impl;

import com.innowise.carrental.dao.BookingDao;
import com.innowise.carrental.db.ConnectionPool;
import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDaoImpl implements BookingDao {

    private static final Logger log = LoggerFactory.getLogger(BookingDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT id, user_id, car_id, date_from, date_to,
                   total_price, status, created_at
            FROM bookings
            WHERE id = ?
            """;

    private static final String FIND_BY_USER_ID_PAGED = """
            SELECT id, user_id, car_id, date_from, date_to,
                   total_price, status, created_at
            FROM bookings
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_USER_ID = """
            SELECT COUNT(*) FROM bookings WHERE user_id = ?
            """;

    private static final String FIND_BY_STATUS_PAGED = """
            SELECT id, user_id, car_id, date_from, date_to,
                   total_price, status, created_at
            FROM bookings
            WHERE status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_STATUS = """
            SELECT COUNT(*) FROM bookings WHERE status = ?
            """;

    private static final String EXISTS_OVERLAPPING = """
            SELECT COUNT(*) FROM bookings
            WHERE car_id = ?
              AND status IN ('PENDING', 'CONFIRMED')
              AND date_from < ?
              AND date_to > ?
            """;

    private static final String SAVE = """
            INSERT INTO bookings (user_id, car_id, date_from, date_to, total_price, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_STATUS = """
            UPDATE bookings SET status = ? WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM bookings WHERE id = ?";

    @Override
    public Optional<Booking> findById(long id) throws DaoException {
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
            log.error("Failed to find booking by id={}", id, e);
            throw new DaoException("Failed to find booking by id: " + id, e);
        }
    }

    @Override
    public List<Booking> findByUserId(long userId, int offset, int limit) throws DaoException {
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USER_ID_PAGED)) {

            statement.setLong(1, userId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find bookings by userId={}", userId, e);
            throw new DaoException("Failed to find bookings by userId: " + userId, e);
        }

        return bookings;
    }

    @Override
    public int countByUserId(long userId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_USER_ID)) {

            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            log.error("Failed to count bookings by userId={}", userId, e);
            throw new DaoException("Failed to count bookings by userId: " + userId, e);
        }
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status, int offset, int limit) throws DaoException {
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_PAGED)) {

            statement.setString(1, status.name());
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find bookings by status={}", status, e);
            throw new DaoException("Failed to find bookings by status: " + status, e);
        }

        return bookings;
    }

    @Override
    public int countByStatus(BookingStatus status) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_STATUS)) {

            statement.setString(1, status.name());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            log.error("Failed to count bookings by status={}", status, e);
            throw new DaoException("Failed to count bookings by status: " + status, e);
        }
    }

    @Override
    public boolean existsOverlapping(long carId, LocalDate dateFrom, LocalDate dateTo) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_OVERLAPPING)) {

            statement.setLong(1, carId);
            // date_from < new.dateTo - existing booking starts before new one ends
            statement.setDate(2, Date.valueOf(dateTo));
            // date_to > new.dateFrom - existing booking ends after new one starts
            statement.setDate(3, Date.valueOf(dateFrom));

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            log.error("Failed to check overlapping bookings carId={}", carId, e);
            throw new DaoException("Failed to check overlapping bookings for car: " + carId, e);
        }
    }

    @Override
    public void save(Booking booking) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, booking.getUserId());
            statement.setLong(2, booking.getCarId());
            statement.setDate(3, Date.valueOf(booking.getDateFrom()));
            statement.setDate(4, Date.valueOf(booking.getDateTo()));
            statement.setBigDecimal(5, booking.getTotalPrice());
            statement.setString(6, booking.getStatus().name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to save booking userId={} carId={}", booking.getUserId(), booking.getCarId(), e);
            throw new DaoException("Failed to save booking", e);
        }
    }

    @Override
    public void updateStatus(long bookingId, BookingStatus status) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {

            statement.setString(1, status.name());
            statement.setLong(2, bookingId);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update status for booking id={}", bookingId, e);
            throw new DaoException("Failed to update booking status: " + bookingId, e);
        }
    }

    @Override
    public void update(Booking booking) throws DaoException {
        throw new UnsupportedOperationException("Use updateStatus(bookingId, status)");
    }

    @Override
    public void delete(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete booking id={}", id, e);
            throw new DaoException("Failed to delete booking: " + id, e);
        }
    }

    @Override
    public List<Booking> findAll() throws DaoException {
        throw new UnsupportedOperationException("Use findByUserId or findByStatus with pagination");
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id"));
        booking.setUserId(rs.getLong("user_id"));
        booking.setCarId(rs.getLong("car_id"));
        booking.setDateFrom(rs.getDate("date_from").toLocalDate());
        booking.setDateTo(rs.getDate("date_to").toLocalDate());
        booking.setTotalPrice(rs.getBigDecimal("total_price"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return booking;
    }

}
