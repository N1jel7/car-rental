package com.innowise.carrental.dao;

import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.exception.DaoException;

import java.time.LocalDate;
import java.util.List;

public interface BookingDao extends BaseDao<Booking> {

    // User's own bookings page.
    List<Booking> findByUserId(long userId, int offset, int limit) throws DaoException;

    int countByUserId(long userId) throws DaoException;

    // Admin: all bookings across all users.
    List<Booking> findByStatus(BookingStatus status, int offset, int limit) throws DaoException;

    int countByStatus(BookingStatus status) throws DaoException;

    // Used in BookingService to check date conflicts before creating a booking.
    // Returns true if the car already has an active booking overlapping the given dates.
    boolean existsOverlapping(long carId, LocalDate dateFrom, LocalDate dateTo) throws DaoException;

    void updateStatus(long bookingId, BookingStatus status) throws DaoException;

}
