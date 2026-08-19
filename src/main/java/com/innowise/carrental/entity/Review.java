package com.innowise.carrental.entity;

import java.time.LocalDateTime;
import java.util.Objects;


public class Review {

    private Long id;
    private Long userId;
    private Long carId;
    private Long bookingId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(Long id, Long userId, Long carId, Long bookingId, int rating,
                  String comment, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.carId = carId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
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

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        private final Review review = new Review();

        public Builder userId(Long userId) {
            review.userId = userId;
            return this;
        }

        public Builder carId(Long carId) {
            review.carId = carId;
            return this;
        }

        public Builder bookingId(Long bookingId) {
            review.bookingId = bookingId;
            return this;
        }

        public Builder rating(int rating) {
            review.rating = rating;
            return this;
        }

        public Builder comment(String comment) {
            review.comment = comment;
            return this;
        }

        public Review build() {
            return review;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Review{id=%d, userId=%d, carId=%d, rating=%d}"
                .formatted(id, userId, carId, rating);
    }
}
