package com.innowise.carrental.dao;

import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.DaoException;

import java.util.List;

public interface CarDao extends BaseDao<Car> {

    // only AVAILABLE cars, with pagination.
    List<Car> findByStatus(CarStatus status, int offset, int limit) throws DaoException;

    int countByStatus(CarStatus status) throws DaoException;

    // search/filter by make.
    List<Car> findByMake(String make) throws DaoException;

    void updateStatus(long carId, CarStatus status) throws DaoException;

}
