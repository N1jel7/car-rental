package com.innowise.carrental.dao;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface UserDao extends BaseDao<User> {

    // Used during sign-in: find user by email, then check password hash.
    Optional<User> findByEmail(String email) throws DaoException;

    // Used by admin: view all registered users.
    List<User> findAll(int offset, int limit) throws DaoException;

    // Total count for pagination.
    int countAll() throws DaoException;
}
