package com.innowise.carrental.dao;

import com.innowise.carrental.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T> {

    void save(T entity) throws DaoException;

    void update(T entity) throws DaoException;

    void delete(long id) throws DaoException;

    Optional<T> findById(long id) throws DaoException;

    List<T> findAll() throws DaoException;

}
