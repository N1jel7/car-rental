package com.innowise.carrental.dao;

import com.innowise.carrental.entity.Review;
import com.innowise.carrental.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface ReviewDao extends BaseDao<Review> {

    // All reviews for a car (shown on car detail page, with pagination).
    List<Review> findByCarId(long carId, int offset, int limit) throws DaoException;

    int countByCarId(long carId) throws DaoException;

    // Check if user already left a review for this specific booking.
    // Enforces the "one review per booking" rule at the service layer.
    Optional<Review> findByBookingId(long bookingId) throws DaoException;

    // Average rating shown on car card in catalog.
    double findAverageRatingByCarId(long carId) throws DaoException;

}
