package com.innowise.carrental.dao;

import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.exception.DaoException;


import java.util.List;
import java.util.Optional;

public interface CarImageDao extends BaseDao<CarImage> {

    // Load all photos for a specific car (for gallery display).
    List<CarImage> findByCarId(long carId) throws DaoException;

    // Returns the primary photo, used in car list cards.
    Optional<CarImage> findPrimaryByCarId(long carId) throws DaoException;

    void deleteByCarId(long carId) throws DaoException;

}
