package com.innowise.carrental.service;

import com.innowise.carrental.dao.BookingDao;
import com.innowise.carrental.dao.CarDao;
import com.innowise.carrental.dao.impl.BookingDaoImpl;
import com.innowise.carrental.dao.impl.CarDaoImpl;
import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingDao bookingDao;
    private final CarDao carDao;

    public BookingService(BookingDao bookingDao, CarDao carDao) {
        this.bookingDao = bookingDao;
        this.carDao = carDao;
    }

    public BookingService() {
        this(new BookingDaoImpl(), new CarDaoImpl());
    }

    // create a booking for a car.
    // Rules:
    // 1. dateFrom must be today or later
    // 2. dateTo must be after dateFrom
    // 3. Car must exist and be AVAILABLE
    // 4. No overlapping active bookings for this car on these dates
    public Booking create(long userId, long carId, LocalDate dateFrom, LocalDate dateTo)
            throws ServiceException, ValidationException {
        validateDates(dateFrom, dateTo);

        try {
            Car car = carDao.findById(carId)
                    .orElseThrow(() -> new ServiceException("Car not found: " + carId));

            if (car.getStatus() != CarStatus.AVAILABLE) {
                throw new ValidationException("Car is not available for booking");
            }

            if (bookingDao.existsOverlapping(carId, dateFrom, dateTo)) {
                throw new ValidationException(
                        "Car is already booked for the selected dates");
            }


            long days = ChronoUnit.DAYS.between(dateFrom, dateTo);
            BigDecimal totalPrice = car.getPricePerDay()
                    .multiply(BigDecimal.valueOf(days));

            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setCarId(carId);
            booking.setDateFrom(dateFrom);
            booking.setDateTo(dateTo);
            booking.setTotalPrice(totalPrice);
            booking.setStatus(BookingStatus.PENDING);

            bookingDao.save(booking);
            log.info("Created booking id={} userId={} carId={}", booking.getId(), userId, carId);
            return booking;

        } catch (DaoException e) {
            log.error("Failed to create booking userId={} carId={}", userId, carId, e);
            throw new ServiceException("Failed to create booking", e);
        }
    }

    // Admin confirms a booking
    public void confirm(long bookingId) throws ServiceException {
        try {
            Booking booking = bookingDao.findById(bookingId)
                    .orElseThrow(() -> new ServiceException("Booking not found: " + bookingId));

            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new ServiceException(
                        "Only PENDING bookings can be confirmed");
            }

            bookingDao.updateStatus(bookingId, BookingStatus.CONFIRMED);
            carDao.updateStatus(booking.getCarId(), CarStatus.BOOKED);
            log.info("Confirmed booking id={}", bookingId);

        } catch (DaoException e) {
            log.error("Failed to confirm booking id={}", bookingId, e);
            throw new ServiceException("Failed to confirm booking", e);
        }
    }

    // User cancels their own booking.
    public void cancel(long bookingId, long requestingUserId)
            throws ServiceException, ValidationException {
        try {
            Booking booking = bookingDao.findById(bookingId)
                    .orElseThrow(() -> new ServiceException("Booking not found: " + bookingId));

            // Security check: user can only cancel their own bookings.
            if (booking.getUserId() != requestingUserId) {
                throw new ServiceException("Access denied: not your booking");
            }

            if (booking.getStatus() == BookingStatus.COMPLETED
                    || booking.getStatus() == BookingStatus.CANCELLED) {
                throw new ValidationException("This booking cannot be cancelled");
            }

            if (!LocalDate.now().isBefore(booking.getDateFrom())) {
                throw new ValidationException(
                        "Cannot cancel a booking after the rental has started");
            }

            bookingDao.updateStatus(bookingId, BookingStatus.CANCELLED);

            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                carDao.updateStatus(booking.getCarId(), CarStatus.AVAILABLE);
            }

            log.info("Cancelled booking id={} by userId={}", bookingId, requestingUserId);

        } catch (DaoException e) {
            log.error("Failed to cancel booking id={}", bookingId, e);
            throw new ServiceException("Failed to cancel booking", e);
        }
    }

    // Admin marks a booking as completed
    public void complete(long bookingId) throws ServiceException {
        try {
            Booking booking = bookingDao.findById(bookingId)
                    .orElseThrow(() -> new ServiceException("Booking not found: " + bookingId));

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new ServiceException(
                        "Only CONFIRMED bookings can be marked as completed");
            }

            bookingDao.updateStatus(bookingId, BookingStatus.COMPLETED);
            carDao.updateStatus(booking.getCarId(), CarStatus.AVAILABLE);
            log.info("Completed booking id={}", bookingId);

        } catch (DaoException e) {
            log.error("Failed to complete booking id={}", bookingId, e);
            throw new ServiceException("Failed to complete booking", e);
        }
    }

    public Booking findById(long bookingId) throws ServiceException {
        try {
            return bookingDao.findById(bookingId)
                    .orElseThrow(() -> new ServiceException("Booking not found: " + bookingId));
        } catch (DaoException e) {
            log.error("Failed to find booking id={}", bookingId, e);
            throw new ServiceException("Failed to find booking", e);
        }
    }

    public List<Booking> findByUser(long userId, int page, int pageSize)
            throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return bookingDao.findByUserId(userId, offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to find bookings for userId={}", userId, e);
            throw new ServiceException("Failed to load bookings", e);
        }
    }

    public int countByUser(long userId) throws ServiceException {
        try {
            return bookingDao.countByUserId(userId);
        } catch (DaoException e) {
            log.error("Failed to count bookings for userId={}", userId, e);
            throw new ServiceException("Failed to count bookings", e);
        }
    }

    public List<Booking> findByStatus(BookingStatus status, int page, int pageSize)
            throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return bookingDao.findByStatus(status, offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to find bookings by status={}", status, e);
            throw new ServiceException("Failed to load bookings", e);
        }
    }

    public int countByStatus(BookingStatus status) throws ServiceException {
        try {
            return bookingDao.countByStatus(status);
        } catch (DaoException e) {
            log.error("Failed to count bookings by status={}", status, e);
            throw new ServiceException("Failed to count bookings", e);
        }
    }


    private void validateDates(LocalDate dateFrom, LocalDate dateTo)
            throws ValidationException {
        if (dateFrom == null || dateTo == null) {
            throw new ValidationException("Dates must not be empty");
        }
        if (!dateFrom.isBefore(dateTo)) {
            throw new ValidationException("Return date must be after pickup date");
        }
        if (dateFrom.isBefore(LocalDate.now())) {
            throw new ValidationException("Pickup date cannot be in the past");
        }
    }

}
