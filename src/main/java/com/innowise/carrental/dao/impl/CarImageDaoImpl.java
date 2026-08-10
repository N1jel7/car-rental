package com.innowise.carrental.dao.impl;

import com.innowise.carrental.dao.CarImageDao;
import com.innowise.carrental.db.ConnectionPool;
import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarImageDaoImpl implements CarImageDao {

    private static final Logger log = LoggerFactory.getLogger(CarImageDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT id, car_id, file_path, is_primary, uploaded_at
            FROM car_images
            WHERE id = ?
            """;

    private static final String FIND_BY_CAR_ID = """
            SELECT id, car_id, file_path, is_primary, uploaded_at
            FROM car_images
            WHERE car_id = ?
            ORDER BY is_primary DESC, uploaded_at ASC
            """;

    private static final String FIND_PRIMARY_BY_CAR_ID = """
            SELECT id, car_id, file_path, is_primary, uploaded_at
            FROM car_images
            WHERE car_id = ? AND is_primary = true
            LIMIT 1
            """;

    private static final String SAVE = """
            INSERT INTO car_images (car_id, file_path, is_primary)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE = "DELETE FROM car_images WHERE id = ?";

    private static final String DELETE_BY_CAR_ID = "DELETE FROM car_images WHERE car_id = ?";

    // If a new primary image is being set, we first reset all others for this car.
    private static final String RESET_PRIMARY = """
            UPDATE car_images SET is_primary = false WHERE car_id = ?
            """;

    @Override
    public Optional<CarImage> findById(long id) throws DaoException {
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
            log.error("Failed to find car image by id={}", id, e);
            throw new DaoException("Failed to find car image by id: " + id, e);
        }
    }

    @Override
    public List<CarImage> findByCarId(long carId) throws DaoException {
        List<CarImage> images = new ArrayList<>();

        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CAR_ID)) {

            statement.setLong(1, carId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    images.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to find images for carId={}", carId, e);
            throw new DaoException("Failed to find images for car: " + carId, e);
        }

        return images;
    }

    @Override
    public Optional<CarImage> findPrimaryByCarId(long carId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PRIMARY_BY_CAR_ID)) {

            statement.setLong(1, carId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            log.error("Failed to find primary image for carId={}", carId, e);
            throw new DaoException("Failed to find primary image for car: " + carId, e);
        }
    }

    // Saving an image is a two-step operation when is_primary=true:
    // first reset all existing primary flags for this car, then insert.
    // Both steps must succeed or neither should (transaction).
    @Override
    public void save(CarImage image) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection()) {

            connection.setAutoCommit(false);

            try {
                if (image.isPrimary()) {
                    try (PreparedStatement resetStatement = connection.prepareStatement(RESET_PRIMARY)) {
                        resetStatement.setLong(1, image.getCarId());
                        resetStatement.executeUpdate();
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, image.getCarId());
                    statement.setString(2, image.getFilePath());
                    statement.setBoolean(3, image.isPrimary());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            image.setId(keys.getLong(1));
                        }
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                log.error("Failed to save car image carId={}", image.getCarId(), e);
                throw new DaoException("Failed to save car image", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            log.error("Failed to get connection for saving car image", e);
            throw new DaoException("Failed to get connection", e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete car image id={}", id, e);
            throw new DaoException("Failed to delete car image: " + id, e);
        }
    }

    @Override
    public void deleteByCarId(long carId) throws DaoException {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_CAR_ID)) {

            statement.setLong(1, carId);
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to delete images for carId={}", carId, e);
            throw new DaoException("Failed to delete images for car: " + carId, e);
        }
    }

    @Override
    public void update(CarImage image) throws DaoException {
        throw new UnsupportedOperationException("Images are not updated — delete and re-upload instead");
    }

    @Override
    public List<CarImage> findAll() throws DaoException {
        throw new UnsupportedOperationException("Use findByCarId(carId)");
    }

    private CarImage mapRow(ResultSet rs) throws SQLException {
        CarImage image = new CarImage();
        image.setId(rs.getLong("id"));
        image.setCarId(rs.getLong("car_id"));
        image.setFilePath(rs.getString("file_path"));
        image.setPrimary(rs.getBoolean("is_primary"));
        image.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        return image;
    }

}
