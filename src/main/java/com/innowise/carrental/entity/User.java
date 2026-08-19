package com.innowise.carrental.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private Role role;
    private String locale;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String email, String passwordHash, String fullName,
                String phone, Role role, String locale,
                LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.locale = locale;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
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
        private final User user = new User();

        public Builder email(String email) {
            user.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            user.passwordHash = passwordHash;
            return this;
        }

        public Builder fullName(String fullName) {
            user.fullName = fullName;
            return this;
        }

        public Builder phone(String phone) {
            user.phone = phone;
            return this;
        }

        public Builder role(Role role) {
            user.role = role;
            return this;
        }

        public Builder locale(String locale) {
            user.locale = locale;
            return this;
        }

        public User build() {
            return user;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{id=%d, email='%s', role=%s}".formatted(id, email, role);
    }
}
