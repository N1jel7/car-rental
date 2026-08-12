package com.innowise.carrental.dao.impl;

import com.innowise.carrental.dao.CarDao;
import com.innowise.carrental.db.ConnectionPool;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDaoImpl implements CarDao {

    private static final Logger log = LoggerFactory.getLogger(CarDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT id, make, model, year, price_per_day, status, description, created_at
            FROM cars
            WHERE id = ?
            """;

    private static final String FIND_BY_STATUS_PAGED = """
            SELECT id, make, model, year, price_per_day, status, description, created_at
            FROM cars
            WHERE status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_BY_STATUS = """
            SELECT COUNT(*) FROM cars WHERE status = ?
            """;

    private static final String FIND_BY_MAKE = """
            SELECT id, make, model, year, price_per_day, status, description, created_at
            FROM cars
            WHERE LOWER(make) = LOWER(?)
            ORDER BY model
            """;

    private static final String SAVE = """
            INSERT INTO cars (make, model, year, price_per_day, status, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE cars
            SET make = ?, model = ?, year = ?, price_per_day = ?, description = ?
            WHERE id = ?
            """;

    private static final String UPDATE_STATUS = """
            UPDATE cars SET status = ? WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM cars WHERE id = ?";

    @Override
    public Optional<Car> findById(long id) throws DaoException {
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
            log.error("Failed to find car by id={}", id, e);
            throw new DaoException("Failed to find car by id: " + id, e);
        }
    }

    @Override
    public List<Car> findByStatus(CarStatus status, int offset, int limit) throws DaoException {
        List<Car> cars = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_PAGED)) {

            statement.setString(1, status.name());
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find cars by status={}", status, e);
            throw new DaoException("Failed to find cars by status: " + status, e);
        }

        return cars;
    }

    @Override
    public int countByStatus(CarStatus status) throws DaoException {
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
            log.error("Failed to count cars by status={}", status, e);
            throw new DaoException("Failed to count cars by status: " + status, e);
        }
    }

    @Override
    public List<Car> findByMake(String make) throws DaoException {
        List<Car> cars = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_MAKE)) {

            statement.setString(1, make);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find cars by make={}", make, e);
            throw new DaoException("Failed to find cars by make: " + make, e);
        }

        return cars;
    }

    @Override
    public List<Car> search(String make, BigDecimal minPrice, BigDecimal maxPrice,
                             Boolean availableOnly, int offset, int limit) throws DaoException {
        List<Car> cars = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT id, make, model, year, price_per_day, status, description, created_at
                FROM cars
                """);
        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, make, minPrice, maxPrice, availableOnly);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bindParams(statement, params);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    cars.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to search cars make={} minPrice={} maxPrice={} availableOnly={}",
                    make, minPrice, maxPrice, availableOnly, e);
            throw new DaoException("Failed to search cars", e);
        }

        return cars;
    }

    @Override
    public int countSearch(String make, BigDecimal minPrice, BigDecimal maxPrice,
                            Boolean availableOnly) throws DaoException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cars");
        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, make, minPrice, maxPrice, availableOnly);

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            bindParams(statement, params);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Failed to count car search make={} minPrice={} maxPrice={} availableOnly={}",
                    make, minPrice, maxPrice, availableOnly, e);
            throw new DaoException("Failed to count car search", e);
        }
    }

    private void appendSearchConditions(StringBuilder sql, List<Object> params, String make,
                                         BigDecimal minPrice, BigDecimal maxPrice, Boolean availableOnly) {
        List<String> conditions = new ArrayList<>();

        if (make != null && !make.isBlank()) {
            conditions.add("LOWER(make) LIKE LOWER(?)");
            params.add("%" + make + "%");
        }
        if (minPrice != null) {
            conditions.add("price_per_day >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            conditions.add("price_per_day <= ?");
            params.add(maxPrice);
        }
        if (availableOnly != null) {
            conditions.add(availableOnly ? "status = 'AVAILABLE'" : "status != 'AVAILABLE'");
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
    }

    private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof BigDecimal bd) {
                statement.setBigDecimal(i + 1, bd);
            } else if (param instanceof Integer n) {
                statement.setInt(i + 1, n);
            } else {
                statement.setString(i + 1, param.toString());
            }
        }
    }

    @Override
    public void updateStatus(long carId, CarStatus status) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {

            statement.setString(1, status.name());
            statement.setLong(2, carId);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update status for car id={}", carId, e);
            throw new DaoException("Failed to update car status: " + carId, e);
        }
    }

    @Override
    public void save(Car car) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, car.getMake());
            statement.setString(2, car.getModel());
            statement.setInt(3, car.getYear());
            statement.setBigDecimal(4, car.getPricePerDay());
            statement.setString(5, car.getStatus().name());
            statement.setString(6, car.getDescription());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    car.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to save car make={} model={}", car.getMake(), car.getModel(), e);
            throw new DaoException("Failed to save car: " + car.getMake() + " " + car.getModel(), e);
        }
    }

    @Override
    public void update(Car car) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, car.getMake());
            statement.setString(2, car.getModel());
            statement.setInt(3, car.getYear());
            statement.setBigDecimal(4, car.getPricePerDay());
            statement.setString(5, car.getDescription());
            statement.setLong(6, car.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update car id={}", car.getId(), e);
            throw new DaoException("Failed to update car: " + car.getId(), e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete car id={}", id, e);
            throw new DaoException("Failed to delete car: " + id, e);
        }
    }

    @Override
    public List<Car> findAll() throws DaoException {
        throw new UnsupportedOperationException("Use findByStatus(status, offset, limit)");
    }

    private Car mapRow(ResultSet rs) throws SQLException {
        Car car = new Car();
        car.setId(rs.getLong("id"));
        car.setMake(rs.getString("make"));
        car.setModel(rs.getString("model"));
        car.setYear(rs.getInt("year"));
        car.setPricePerDay(rs.getBigDecimal("price_per_day"));
        car.setStatus(CarStatus.valueOf(rs.getString("status")));
        car.setDescription(rs.getString("description"));
        car.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return car;
    }

}
