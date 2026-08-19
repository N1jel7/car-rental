package com.innowise.carrental.service;

import com.innowise.carrental.dao.CarDao;
import com.innowise.carrental.dao.CarImageDao;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarStatus;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarDao carDao;

    @Mock
    private CarImageDao carImageDao;

    private CarService carService;

    private CarService service() {
        return new CarService(carDao, carImageDao);
    }

    @Test
    void add_validData_savesCarAsAvailable() throws Exception {
        // given
        carService = service();

        // when
        Car result = carService.add("Toyota", "Camry", 2022, new BigDecimal("75.00"), "Comfortable sedan");

        // then
        assertEquals("Toyota", result.getMake());
        assertEquals("Camry", result.getModel());
        assertEquals(CarStatus.AVAILABLE, result.getStatus());
        verify(carDao).save(result);
    }

    @Test
    void add_blankMake_throwsValidationExceptionAndDoesNotSave() throws Exception {
        // given
        carService = service();

        // when / then
        assertThrows(ValidationException.class, () ->
                carService.add(" ", "Camry", 2022, new BigDecimal("75.00"), null));
        verify(carDao, never()).save(any());
    }

    @Test
    void add_yearInTheFuture_throwsValidationException() {
        // given
        carService = service();
        int nextYear = Year.now().getValue() + 2;

        // when / then
        assertThrows(ValidationException.class, () ->
                carService.add("Toyota", "Camry", nextYear, new BigDecimal("75.00"), null));
    }

    @Test
    void add_negativePrice_throwsValidationException() {
        // given
        carService = service();

        // when / then
        assertThrows(ValidationException.class, () ->
                carService.add("Toyota", "Camry", 2022, new BigDecimal("-10.00"), null));
    }

    @Test
    void update_validData_appliesChangesAndPersists() throws Exception {
        // given
        carService = service();
        Car existing = Car.builder()
                .make("Toyota")
                .model("Camry")
                .year(2020)
                .pricePerDay(new BigDecimal("60.00"))
                .status(CarStatus.AVAILABLE)
                .build();
        when(carDao.findById(1L)).thenReturn(Optional.of(existing));

        // when
        carService.update(1L, "Toyota", "Camry", 2023, new BigDecimal("80.00"), "Updated");

        // then
        assertEquals(2023, existing.getYear());
        assertEquals(new BigDecimal("80.00"), existing.getPricePerDay());
        verify(carDao).update(existing);
    }

    @Test
    void update_carNotFound_throwsServiceException() throws Exception {
        // given
        carService = service();
        when(carDao.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ServiceException.class, () ->
                carService.update(99L, "Toyota", "Camry", 2023, new BigDecimal("80.00"), null));
    }

    @Test
    void delete_removesImagesBeforeCar() throws Exception {
        // given
        carService = service();

        // when
        carService.delete(1L);

        // then
        verify(carImageDao).deleteByCarId(1L);
        verify(carDao).delete(1L);
    }

    @Test
    void findById_carDoesNotExist_throwsServiceException() throws Exception {
        // given
        carService = service();
        when(carDao.findById(404L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ServiceException.class, () -> carService.findById(404L));
    }

    @Test
    void addImage_firstImage_savesAsPrimary() throws Exception {
        // given
        carService = service();

        // when
        carService.addImage(1L, "cars/photo.jpg", true);

        // then
        verify(carImageDao).save(argThat(image ->
                image.getCarId().equals(1L)
                        && image.getFilePath().equals("cars/photo.jpg")
                        && image.isPrimary()));
    }

}
