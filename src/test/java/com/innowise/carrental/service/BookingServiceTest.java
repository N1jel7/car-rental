package com.innowise.carrental.service;

import com.innowise.carrental.dao.BookingDao;
import com.innowise.carrental.dao.CarDao;
import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingDao bookingDao;

    @Mock
    private CarDao carDao;

    private BookingService bookingService;

    private BookingService service() {
        return new BookingService(bookingDao, carDao);
    }

    private Car availableCar() {
        return Car.builder()
                .make("Toyota")
                .model("Camry")
                .pricePerDay(new BigDecimal("50.00"))
                .status(CarStatus.AVAILABLE)
                .build();
    }

    @Test
    void create_availableCarAndFreeDates_savesPendingBookingWithComputedPrice() throws Exception {
        // given
        bookingService = service();
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(4);
        when(carDao.findById(1L)).thenReturn(Optional.of(availableCar()));
        when(bookingDao.existsOverlapping(1L, from, to)).thenReturn(false);

        // when
        Booking result = bookingService.create(10L, 1L, from, to);

        // then
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
        verify(bookingDao).save(result);
    }

    @Test
    void create_carNotAvailable_throwsValidationExceptionAndDoesNotSave() throws Exception {
        // given
        bookingService = service();
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(4);
        Car bookedCar = Car.builder().status(CarStatus.BOOKED).pricePerDay(BigDecimal.TEN).build();
        when(carDao.findById(1L)).thenReturn(Optional.of(bookedCar));

        // when / then
        assertThrows(ValidationException.class, () -> bookingService.create(10L, 1L, from, to));
        verify(bookingDao, never()).save(any());
    }

    @Test
    void create_overlappingDates_throwsValidationException() throws Exception {
        // given
        bookingService = service();
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(4);
        when(carDao.findById(1L)).thenReturn(Optional.of(availableCar()));
        when(bookingDao.existsOverlapping(1L, from, to)).thenReturn(true);

        // when / then
        assertThrows(ValidationException.class, () -> bookingService.create(10L, 1L, from, to));
    }

    @Test
    void create_pickupDateInThePast_throwsValidationExceptionBeforeTouchingDao() {
        // given
        bookingService = service();
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(2);

        // when / then
        assertThrows(ValidationException.class, () -> bookingService.create(10L, 1L, from, to));
        verifyNoInteractions(bookingDao, carDao);
    }

    @Test
    void create_returnDateNotAfterPickupDate_throwsValidationException() {
        // given
        bookingService = service();
        LocalDate from = LocalDate.now().plusDays(3);
        LocalDate to = LocalDate.now().plusDays(3);

        // when / then
        assertThrows(ValidationException.class, () -> bookingService.create(10L, 1L, from, to));
    }

    @Test
    void confirm_pendingBooking_updatesBookingAndCarStatus() throws Exception {
        // given
        bookingService = service();
        Booking pending = Booking.builder().carId(5L).status(BookingStatus.PENDING).build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(pending));

        // when
        bookingService.confirm(1L);

        // then
        verify(bookingDao).updateStatus(1L, BookingStatus.CONFIRMED);
        verify(carDao).updateStatus(5L, CarStatus.BOOKED);
    }

    @Test
    void confirm_alreadyConfirmedBooking_throwsServiceExceptionAndDoesNotUpdate() throws Exception {
        // given
        bookingService = service();
        Booking confirmed = Booking.builder().carId(5L).status(BookingStatus.CONFIRMED).build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(confirmed));

        // when / then
        assertThrows(ServiceException.class, () -> bookingService.confirm(1L));
        verify(bookingDao, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancel_notTheOwner_throwsServiceExceptionAndDoesNotCancel() throws Exception {
        // given
        bookingService = service();
        Booking booking = Booking.builder()
                .userId(1L)
                .carId(5L)
                .status(BookingStatus.PENDING)
                .dateFrom(LocalDate.now().plusDays(2))
                .build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        // when / then
        assertThrows(ServiceException.class, () -> bookingService.cancel(1L, 999L));
        verify(bookingDao, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancel_alreadyCompletedBooking_throwsValidationException() throws Exception {
        // given
        bookingService = service();
        Booking booking = Booking.builder()
                .userId(1L)
                .carId(5L)
                .status(BookingStatus.COMPLETED)
                .build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        // when / then
        assertThrows(ValidationException.class, () -> bookingService.cancel(1L, 1L));
    }

    @Test
    void cancel_confirmedBookingBeforeStart_freesUpTheCar() throws Exception {
        // given
        bookingService = service();
        Booking booking = Booking.builder()
                .userId(1L)
                .carId(5L)
                .status(BookingStatus.CONFIRMED)
                .dateFrom(LocalDate.now().plusDays(3))
                .build();
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        // when
        bookingService.cancel(1L, 1L);

        // then
        verify(bookingDao).updateStatus(1L, BookingStatus.CANCELLED);
        verify(carDao).updateStatus(5L, CarStatus.AVAILABLE);
    }

}
