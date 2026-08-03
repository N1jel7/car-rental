package com.innowise.carrental.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class CarImage {

    private Long id;
    private Long carId;
    private String filePath;
    private boolean primary;
    private LocalDateTime uploadedAt;

    public CarImage() {
    }

    public CarImage(Long id, Long carId, String filePath, boolean primary, LocalDateTime uploadedAt) {
        this.id = id;
        this.carId = carId;
        this.filePath = filePath;
        this.primary = primary;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarImage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CarImage{id=%d, carId=%d, filePath='%s', primary=%b}"
                .formatted(id, carId, filePath, primary);
    }
}
