package com.innowise.carrental.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


public class Booking {

    private Long id;
    private Long userId;
    private Long carId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public Booking() {
    }

    public Booking(Long id, Long userId, Long carId, LocalDate dateFrom, LocalDate dateTo,
                   BigDecimal totalPrice, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.carId = carId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Booking booking = new Booking();

        public Builder userId(Long userId) {
            booking.userId = userId;
            return this;
        }

        public Builder carId(Long carId) {
            booking.carId = carId;
            return this;
        }

        public Builder dateFrom(LocalDate dateFrom) {
            booking.dateFrom = dateFrom;
            return this;
        }

        public Builder dateTo(LocalDate dateTo) {
            booking.dateTo = dateTo;
            return this;
        }

        public Builder totalPrice(BigDecimal totalPrice) {
            booking.totalPrice = totalPrice;
            return this;
        }

        public Builder status(BookingStatus status) {
            booking.status = status;
            return this;
        }

        public Booking build() {
            return booking;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Booking{id=%d, userId=%d, carId=%d, dateFrom=%s, dateTo=%s, status=%s}"
                .formatted(id, userId, carId, dateFrom, dateTo, status);
    }
}
