package com.innowise.carrental.service;

import com.innowise.carrental.dao.BookingDao;
import com.innowise.carrental.dao.ReviewDao;
import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Review;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private BookingDao bookingDao;

    private ReviewService reviewService;

    private ReviewService service() {
        return new ReviewService(reviewDao, bookingDao);
    }

    private Booking completedBookingFor(long userId, long carId) {
        return Booking.builder()
                .userId(userId)
                .carId(carId)
                .status(BookingStatus.COMPLETED)
                .build();
    }

    @Test
    void create_completedBookingNotYetReviewed_savesReview() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));
        when(reviewDao.findByBookingId(1L)).thenReturn(Optional.empty());

        // when
        Review result = reviewService.create(10L, 1L, 5, "Great car, smooth ride");

        // then
        assertEquals(5L, result.getCarId());
        assertEquals(5, result.getRating());
        verify(reviewDao).save(result);
    }

    @Test
    void create_bookingNotCompleted_throwsValidationExceptionAndDoesNotSave() throws Exception {
        // given
        reviewService = service();
        Booking pending = Booking.builder().userId(10L).carId(5L).status(BookingStatus.PENDING).build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(pending));

        // when / then
        assertThrows(ValidationException.class, () ->
                reviewService.create(10L, 1L, 5, "Great car"));
        verify(reviewDao, never()).save(any());
    }

    @Test
    void create_bookingAlreadyReviewed_throwsValidationException() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));
        when(reviewDao.findByBookingId(1L)).thenReturn(Optional.of(new Review()));

        // when / then
        assertThrows(ValidationException.class, () ->
                reviewService.create(10L, 1L, 5, "Great car"));
        verify(reviewDao, never()).save(any());
    }

    @Test
    void create_notTheBookingOwner_throwsServiceException() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));

        // when / then
        assertThrows(ServiceException.class, () ->
                reviewService.create(999L, 1L, 5, "Great car"));
    }

    @Test
    void create_ratingOutOfRange_throwsValidationExceptionBeforeTouchingDao() {
        // given
        reviewService = service();

        // when / then
        assertThrows(ValidationException.class, () ->
                reviewService.create(10L, 1L, 6, "Great car"));
    }

    @Test
    void create_blankComment_throwsValidationException() {
        // given
        reviewService = service();

        // when / then
        assertThrows(ValidationException.class, () ->
                reviewService.create(10L, 1L, 5, "   "));
    }

    @Test
    void canReview_eligibleCompletedBookingWithoutReview_returnsTrue() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));
        when(reviewDao.findByBookingId(1L)).thenReturn(Optional.empty());

        // when
        boolean result = reviewService.canReview(10L, 1L);

        // then
        assertTrue(result);
    }

    @Test
    void canReview_bookingNotOwnedByUser_returnsFalse() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));

        // when
        boolean result = reviewService.canReview(999L, 1L);

        // then
        assertFalse(result);
    }

    @Test
    void canReview_alreadyReviewedBooking_returnsFalse() throws Exception {
        // given
        reviewService = service();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(completedBookingFor(10L, 5L)));
        when(reviewDao.findByBookingId(1L)).thenReturn(Optional.of(new Review()));

        // when
        boolean result = reviewService.canReview(10L, 1L);

        // then
        assertFalse(result);
    }

}
