package com.innowise.carrental.web;

import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.service.CarService;
import com.innowise.carrental.service.ReviewService;
import com.innowise.carrental.util.ParseUtil;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CarListServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CarListServlet.class);
    private static final String CARS_PAGE = "cars/list";
    private static final int PAGE_SIZE = 6;

    private CarService carService;
    private ReviewService reviewService;

    @Override
    public void init() {
        carService = new CarService();
        reviewService = new ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = ServletUtil.parsePageParam(request.getParameter("page"));

        String make = ParseUtil.trimToNull(request.getParameter("make"));
        BigDecimal minPrice = ParseUtil.parsePriceOrNull(request.getParameter("minPrice"));
        BigDecimal maxPrice = ParseUtil.parsePriceOrNull(request.getParameter("maxPrice"));
        Boolean availableOnly = ParseUtil.parseAvailability(request.getParameter("status"));

        try {
            List<Car> cars = carService.search(make, minPrice, maxPrice, availableOnly, page, PAGE_SIZE);
            int totalCars = carService.countSearch(make, minPrice, maxPrice, availableOnly);
            int totalPages = (int) Math.ceil((double) totalCars / PAGE_SIZE);

            Map<Long, CarImage> primaryImages = new HashMap<>();
            Map<Long, Double> averageRatings = new HashMap<>();
            Map<Long, Integer> reviewCounts = new HashMap<>();
            for (Car car : cars) {
                Optional<CarImage> image = carService.findPrimaryImage(car.getId());
                image.ifPresent(img -> primaryImages.put(car.getId(), img));

                int reviewCount = reviewService.countByCar(car.getId());
                reviewCounts.put(car.getId(), reviewCount);
                if (reviewCount > 0) {
                    averageRatings.put(car.getId(), reviewService.getAverageRating(car.getId()));
                }
            }

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());

            context.setVariable("cars", cars);
            context.setVariable("primaryImages", primaryImages);
            context.setVariable("averageRatings", averageRatings);
            context.setVariable("reviewCounts", reviewCounts);
            context.setVariable("currentPage", page);
            context.setVariable("totalPages", totalPages);
            context.setVariable("filterMake", make);
            context.setVariable("filterMinPrice", request.getParameter("minPrice"));
            context.setVariable("filterMaxPrice", request.getParameter("maxPrice"));
            context.setVariable("filterStatus", request.getParameter("status"));

            ServletUtil.render(CARS_PAGE, context, response, getServletContext());

        } catch (ServiceException e) {
            log.error("Failed to load car catalog page={}", page, e);

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("error", "Failed to load cars. Please try again.");
            ServletUtil.render(CARS_PAGE, context, response, getServletContext());
        }
    }

}
