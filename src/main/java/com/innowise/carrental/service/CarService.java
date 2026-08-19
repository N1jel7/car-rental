package com.innowise.carrental.service;

import com.innowise.carrental.dao.CarDao;
import com.innowise.carrental.dao.CarImageDao;
import com.innowise.carrental.dao.impl.CarDaoImpl;
import com.innowise.carrental.dao.impl.CarImageDaoImpl;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CarService {

    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    private final CarDao carDao;
    private final CarImageDao carImageDao;

    public CarService(CarDao carDao, CarImageDao carImageDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
    }

    public CarService() {
        this(new CarDaoImpl(), new CarImageDaoImpl());
    }

    public List<Car> findAvailable(int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return carDao.findByStatus(CarStatus.AVAILABLE, offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to find available cars page={}", page, e);
            throw new ServiceException("Failed to load cars", e);
        }
    }

    public int countAvailable() throws ServiceException {
        try {
            return carDao.countByStatus(CarStatus.AVAILABLE);
        } catch (DaoException e) {
            log.error("Failed to count available cars", e);
            throw new ServiceException("Failed to count cars", e);
        }
    }

    // Public catalog search — shows cars of any status; availableOnly narrows to free/occupied.
    public List<Car> search(String make, BigDecimal minPrice, BigDecimal maxPrice,
                             Boolean availableOnly, int page, int pageSize) throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return carDao.search(make, minPrice, maxPrice, availableOnly, offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to search cars make={} minPrice={} maxPrice={} availableOnly={}",
                    make, minPrice, maxPrice, availableOnly, e);
            throw new ServiceException("Failed to search cars", e);
        }
    }

    public int countSearch(String make, BigDecimal minPrice, BigDecimal maxPrice,
                            Boolean availableOnly) throws ServiceException {
        try {
            return carDao.countSearch(make, minPrice, maxPrice, availableOnly);
        } catch (DaoException e) {
            log.error("Failed to count car search make={} minPrice={} maxPrice={} availableOnly={}",
                    make, minPrice, maxPrice, availableOnly, e);
            throw new ServiceException("Failed to count car search", e);
        }
    }

    public Car findById(long id) throws ServiceException {
        try {
            return carDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Car not found: " + id));
        } catch (DaoException e) {
            log.error("Failed to find car id={}", id, e);
            throw new ServiceException("Failed to find car", e);
        }
    }

    public List<Car> findByMake(String make) throws ServiceException {
        try {
            return carDao.findByMake(make);
        } catch (DaoException e) {
            log.error("Failed to find cars by make={}", make, e);
            throw new ServiceException("Failed to find cars by make", e);
        }
    }

    public Car add(String make, String model, int year, BigDecimal pricePerDay, String description)
            throws ServiceException, ValidationException {
        validateMake(make);
        validateModel(model);
        validateYear(year);
        validatePrice(pricePerDay);

        try {
            Car car = new Car();
            car.setMake(make.strip());
            car.setModel(model.strip());
            car.setYear(year);
            car.setPricePerDay(pricePerDay);
            car.setStatus(CarStatus.AVAILABLE);
            car.setDescription(description != null ? description.strip() : null);

            carDao.save(car);
            log.info("Added new car make={} model={}", make, model);
            return car;

        } catch (DaoException e) {
            log.error("Failed to add car make={} model={}", make, model, e);
            throw new ServiceException("Failed to add car", e);
        }
    }

    public void update(long carId, String make, String model, int year, BigDecimal pricePerDay, String description)
            throws ServiceException, ValidationException {
        validateMake(make);
        validateModel(model);
        validateYear(year);
        validatePrice(pricePerDay);

        try {
            Car car = carDao.findById(carId)
                    .orElseThrow(() -> new ServiceException("Car not found: " + carId));

            car.setMake(make.strip());
            car.setModel(model.strip());
            car.setYear(year);
            car.setPricePerDay(pricePerDay);
            car.setDescription(description != null ? description.strip() : null);

            carDao.update(car);
            log.info("Updated car id={}", carId);

        } catch (DaoException e) {
            log.error("Failed to update car id={}", carId, e);
            throw new ServiceException("Failed to update car", e);
        }
    }

    public void delete(long carId) throws ServiceException {
        try {
            carImageDao.deleteByCarId(carId);
            carDao.delete(carId);
            log.info("Deleted car id={}", carId);

        } catch (DaoException e) {
            log.error("Failed to delete car id={}", carId, e);
            throw new ServiceException(
                    "Cannot delete car — it may have active bookings", e);
        }
    }

    public void updateStatus(long carId, CarStatus status) throws ServiceException {
        try {
            carDao.updateStatus(carId, status);
            log.info("Updated status for car id={} status={}", carId, status);
        } catch (DaoException e) {
            log.error("Failed to update status for car id={}", carId, e);
            throw new ServiceException("Failed to update car status", e);
        }
    }

    public void addImage(long carId, String filePath, boolean isPrimary)
            throws ServiceException {
        try {
            CarImage image = new CarImage();
            image.setCarId(carId);
            image.setFilePath(filePath);
            image.setPrimary(isPrimary);

            carImageDao.save(image);
            log.info("Added image for carId={} primary={}", carId, isPrimary);

        } catch (DaoException e) {
            log.error("Failed to add image for carId={}", carId, e);
            throw new ServiceException("Failed to add car image", e);
        }
    }

    public void deleteImage(long imageId) throws ServiceException {
        try {
            carImageDao.findById(imageId).ifPresent(image ->
                    com.innowise.carrental.util.FileUploadUtil.delete(
                            image.getFilePath(), com.innowise.carrental.util.FileUploadUtil.getUploadsRoot()));

            carImageDao.delete(imageId);
            log.info("Deleted image id={}", imageId);
        } catch (DaoException e) {
            log.error("Failed to delete image id={}", imageId, e);
            throw new ServiceException("Failed to delete car image", e);
        }
    }

    public void setPrimaryImage(long carId, long imageId) throws ServiceException {
        try {
            carImageDao.setPrimary(imageId, carId);
            log.info("Set primary image id={} for carId={}", imageId, carId);
        } catch (DaoException e) {
            log.error("Failed to set primary image id={} for carId={}", imageId, carId, e);
            throw new ServiceException("Failed to set primary image", e);
        }
    }

    public List<CarImage> findImages(long carId) throws ServiceException {
        try {
            return carImageDao.findByCarId(carId);
        } catch (DaoException e) {
            log.error("Failed to find images for carId={}", carId, e);
            throw new ServiceException("Failed to load car images", e);
        }
    }

    public Optional<CarImage> findPrimaryImage(long carId) throws ServiceException {
        try {
            return carImageDao.findPrimaryByCarId(carId);
        } catch (DaoException e) {
            log.error("Failed to find primary image for carId={}", carId, e);
            throw new ServiceException("Failed to load primary image", e);
        }
    }

    private void validateMake(String make) throws ValidationException {
        ValidatorUtil.notBlank(make, "Car make must not be empty");
    }

    private void validateModel(String model) throws ValidationException {
        ValidatorUtil.notBlank(model, "Car model must not be empty");
    }

    private void validateYear(int year) throws ValidationException {
        int currentYear = java.time.Year.now().getValue();
        ValidatorUtil.inRange(year, 1900, currentYear + 1,
                "Car year must be between 1900 and " + (currentYear + 1));
    }

    private void validatePrice(BigDecimal price) throws ValidationException {
        ValidatorUtil.positive(price, "Price per day must be greater than 0");
    }

}
