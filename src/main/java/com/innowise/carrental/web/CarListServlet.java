package com.innowise.carrental.web;

import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.service.CarService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/cars")
public class CarListServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CarListServlet.class);
    private static final String CARS_PAGE = "/WEB-INF/templates/cars/list.html";
    private static final int PAGE_SIZE = 6;

    private CarService carService;

    @Override
    public void init() {
        carService = new CarService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = parsePageParam(request.getParameter("page"));

        try {
            List<Car> cars = carService.findAvailable(page, PAGE_SIZE);
            int totalCars = carService.countAvailable();
            int totalPages = (int) Math.ceil((double) totalCars / PAGE_SIZE);


            Map<Long, CarImage> primaryImages = new HashMap<>();
            for (Car car : cars) {
                Optional<CarImage> image = carService.findPrimaryImage(car.getId());
                image.ifPresent(img -> primaryImages.put(car.getId(), img));
            }

            request.setAttribute("cars", cars);
            request.setAttribute("primaryImages", primaryImages);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);

            request.getRequestDispatcher(CARS_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Failed to load car catalog page={}", page, e);
            request.setAttribute("error", "Failed to load cars. Please try again.");
            request.getRequestDispatcher(CARS_PAGE).forward(request, response);
        }
    }

    // Safely parse page parameter
    private int parsePageParam(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return page > 0 ? page : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

}
