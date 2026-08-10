package com.innowise.carrental.web;

import com.innowise.carrental.entity.*;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.BookingService;
import com.innowise.carrental.service.CarService;
import com.innowise.carrental.service.ReviewService;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet("/cars/*")
public class CarDetailServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CarDetailServlet.class);
    private static final String DETAIL_PAGE = "/WEB-INF/templates/cars/detail.html";
    private static final int REVIEWS_PAGE_SIZE = 5;

    private CarService carService;
    private BookingService bookingService;
    private ReviewService reviewService;

    @Override
    public void init() {
        carService = new CarService();
        bookingService = new BookingService();
        reviewService = new ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long carId = parseCarId(request, response);
        if (carId == null) {
            return;
        }

        int reviewPage = ServletUtil.parsePageParam(request.getParameter("reviewPage"));

        try {
            Car car = carService.findById(carId);
            List<CarImage> images = carService.findImages(carId);
            List<Review> reviews = reviewService.findByCar(carId, reviewPage, REVIEWS_PAGE_SIZE);
            int totalReviews = reviewService.countByCar(carId);
            int totalReviewPages = (int) Math.ceil((double) totalReviews / REVIEWS_PAGE_SIZE);
            double averageRating = reviewService.getAverageRating(carId);

            request.setAttribute("car", car);
            request.setAttribute("images", images);
            request.setAttribute("reviews", reviews);
            request.setAttribute("averageRating", averageRating);
            request.setAttribute("reviewPage", reviewPage);
            request.setAttribute("totalReviewPages", totalReviewPages);

            // Check if the logged-in user has a completed booking for this car
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute(AuthFilter.SESSION_USER);
                if (user != null) {
                    // Find user's completed bookings for this car to check review eligibility
                    List<Booking> userBookings = bookingService.findByUser(
                            user.getId(), 1, Integer.MAX_VALUE);

                    boolean canReview = userBookings.stream()
                            .filter(b -> b.getCarId().equals(carId))
                            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                            .anyMatch(b -> {
                                try {
                                    return reviewService.canReview(user.getId(), b.getId());
                                } catch (ServiceException ex) {
                                    return false;
                                }
                            });

                    request.setAttribute("canReview", canReview);

                    // Find the completed booking id to attach the review to
                    userBookings.stream()
                            .filter(b -> b.getCarId().equals(carId))
                            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                            .findFirst()
                            .ifPresent(b -> request.setAttribute("completedBookingId", b.getId()));
                }
            }

            request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Failed to load car detail carId={}", carId, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Car not found");
        }
    }

    // Extract car id from URL path: /cars/42 -> 42
    private Long parseCarId(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/cars");
            return null;
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid car id");
            return null;
        }
    }

}
