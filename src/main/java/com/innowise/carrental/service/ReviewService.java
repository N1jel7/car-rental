package com.innowise.carrental.service;

import com.innowise.carrental.dao.BookingDao;
import com.innowise.carrental.dao.ReviewDao;
import com.innowise.carrental.dao.impl.BookingDaoImpl;
import com.innowise.carrental.dao.impl.ReviewDaoImpl;
import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Review;
import com.innowise.carrental.exception.DaoException;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewDao reviewDao;
    private final BookingDao bookingDao;

    public ReviewService(ReviewDao reviewDao, BookingDao bookingDao) {
        this.reviewDao = reviewDao;
        this.bookingDao = bookingDao;
    }

    public ReviewService() {
        this(new ReviewDaoImpl(), new BookingDaoImpl());
    }

    // rules:
    // 1. Booking must exist and belong to the requesting user
    // 2. Booking must be COMPLETED — review only after the rental is done
    // 3. No review has been left for this booking yet (one review per booking)
    // 4. Rating must be between 1 and 5
    // 5. Comment must not be empty
    public Review create(long userId, long bookingId, int rating, String comment)
            throws ServiceException, ValidationException {

        validateRating(rating);
        validateComment(comment);

        try {
            Booking booking = bookingDao.findById(bookingId)
                    .orElseThrow(() -> new ServiceException("Booking not found: " + bookingId));


            if (booking.getUserId() != userId) {
                throw new ServiceException("Access denied: not your booking");
            }

            if (booking.getStatus() != BookingStatus.COMPLETED) {
                throw new ValidationException(
                        "You can only leave a review after the rental is completed");
            }

            Optional<Review> existing = reviewDao.findByBookingId(bookingId);
            if (existing.isPresent()) {
                throw new ValidationException(
                        "You have already left a review for this booking");
            }

            Review review = new Review();
            review.setUserId(userId);
            review.setCarId(booking.getCarId());
            review.setBookingId(bookingId);
            review.setRating(rating);
            review.setComment(comment.strip());

            reviewDao.save(review);
            log.info("Created review id={} userId={} bookingId={}",
                    review.getId(), userId, bookingId);
            return review;

        } catch (DaoException e) {
            log.error("Failed to create review userId={} bookingId={}", userId, bookingId, e);
            throw new ServiceException("Failed to create review", e);
        }
    }

    public List<Review> findByCar(long carId, int page, int pageSize)
            throws ServiceException {
        try {
            int offset = (page - 1) * pageSize;
            return reviewDao.findByCarId(carId, offset, pageSize);
        } catch (DaoException e) {
            log.error("Failed to find reviews for carId={}", carId, e);
            throw new ServiceException("Failed to load reviews", e);
        }
    }

    public int countByCar(long carId) throws ServiceException {
        try {
            return reviewDao.countByCarId(carId);
        } catch (DaoException e) {
            log.error("Failed to count reviews for carId={}", carId, e);
            throw new ServiceException("Failed to count reviews", e);
        }
    }

    public double getAverageRating(long carId) throws ServiceException {
        try {
            return reviewDao.findAverageRatingByCarId(carId);
        } catch (DaoException e) {
            log.error("Failed to get average rating for carId={}", carId, e);
            throw new ServiceException("Failed to get average rating", e);
        }
    }

    // Check if the user can leave a review for a specific booking
    public boolean canReview(long userId, long bookingId) throws ServiceException {
        try {
            Optional<Booking> booking = bookingDao.findById(bookingId);
            if (booking.isEmpty()) {
                return false;
            }
            if (booking.get().getUserId() != userId) {
                return false;
            }
            if (booking.get().getStatus() != BookingStatus.COMPLETED) {
                return false;
            }
            return reviewDao.findByBookingId(bookingId).isEmpty();
        } catch (DaoException e) {
            log.error("Failed to check review eligibility userId={} bookingId={}",
                    userId, bookingId, e);
            throw new ServiceException("Failed to check review eligibility", e);
        }
    }

    public void delete(long reviewId) throws ServiceException {
        try {
            reviewDao.delete(reviewId);
            log.info("Deleted review id={}", reviewId);
        } catch (DaoException e) {
            log.error("Failed to delete review id={}", reviewId, e);
            throw new ServiceException("Failed to delete review", e);
        }
    }

    private void validateRating(int rating) throws ValidationException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment) throws ValidationException {
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("Comment must not be empty");
        }
        if (comment.length() > 1000) {
            throw new ValidationException("Comment must not exceed 1000 characters");
        }
    }

}
