package com.innowise.carrental.web;

import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.service.BookingService;
import com.innowise.carrental.service.CarService;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminServlet.class);
    private static final String ADMIN_CARS_PAGE = "/WEB-INF/templates/admin/cars.html";
    private static final String ADMIN_BOOKINGS_PAGE = "/WEB-INF/templates/admin/bookings.html";
    private static final String ADMIN_CAR_FORM_PAGE = "/WEB-INF/templates/admin/car-form.html";
    private static final int PAGE_SIZE = 10;

    private CarService carService;
    private BookingService bookingService;

    @Override
    public void init() {
        carService = new CarService();
        bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/cars" -> showCars(request, response);
            case "/cars/new" -> showCarForm(request, response, null);
            case "/bookings" -> showBookings(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/cars/create" -> handleCarCreate(request, response);
            case "/cars/update" -> handleCarUpdate(request, response);
            case "/cars/delete" -> handleCarDelete(request, response);
            case "/bookings/confirm" -> handleBookingConfirm(request, response);
            case "/bookings/complete" -> handleBookingComplete(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // --- GET handlers

    private void showCars(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = ServletUtil.parsePageParam(request.getParameter("page"));

        try {
            List<Car> cars = carService.findAvailable(page, PAGE_SIZE);
            int total = carService.countAvailable();
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            request.setAttribute("cars", cars);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);

            request.getRequestDispatcher(ADMIN_CARS_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Admin failed to load cars", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void showCarForm(HttpServletRequest request, HttpServletResponse response,
                             Car car) throws ServletException, IOException {
        request.setAttribute("car", car);
        request.getRequestDispatcher(ADMIN_CAR_FORM_PAGE).forward(request, response);
    }

    private void showBookings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = ServletUtil.parsePageParam(request.getParameter("page"));
        String statusParam = request.getParameter("status");

        try {
            List<Booking> bookings;
            int total;

            if (statusParam != null && !statusParam.isBlank()) {

                BookingStatus status = BookingStatus.valueOf(statusParam);
                bookings = bookingService.findByStatus(status, page, PAGE_SIZE);
                total = bookingService.countByStatus(status);

            } else {

                bookings = bookingService.findByStatus(BookingStatus.PENDING, page, PAGE_SIZE);
                total = bookingService.countByStatus(BookingStatus.PENDING);

            }

            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            request.setAttribute("bookings", bookings);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("selectedStatus", statusParam);

            request.getRequestDispatcher(ADMIN_BOOKINGS_PAGE).forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/admin/bookings");
        } catch (ServiceException e) {
            log.error("Admin failed to load bookings", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // --- POST handlers

    private void handleCarCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            Car car = carService.add(
                    request.getParameter("make"),
                    request.getParameter("model"),
                    Integer.parseInt(request.getParameter("year")),
                    new BigDecimal(request.getParameter("pricePerDay")),
                    request.getParameter("description")
            );

            log.info("Admin created car id={}", car.getId());
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?success=car_created");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars/new?error=" + encode(e.getMessage()));

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars/new?error=invalid_number");

        } catch (ServiceException e) {
            log.error("Admin failed to create car", e);
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=create_failed");
        }
    }

    private void handleCarUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String carIdParam = request.getParameter("carId");

        try {
            long carId = Long.parseLong(carIdParam);
            carService.update(
                    carId,
                    request.getParameter("make"),
                    request.getParameter("model"),
                    Integer.parseInt(request.getParameter("year")),
                    new BigDecimal(request.getParameter("pricePerDay")),
                    request.getParameter("description")
            );

            log.info("Admin updated car id={}", carId);
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?success=car_updated");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=" + encode(e.getMessage()));

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=invalid_number");

        } catch (ServiceException e) {
            log.error("Admin failed to update car id={}", carIdParam, e);
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=update_failed");
        }
    }

    private void handleCarDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String carIdParam = request.getParameter("carId");

        try {
            long carId = Long.parseLong(carIdParam);
            carService.delete(carId);

            log.info("Admin deleted car id={}", carId);
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?success=car_deleted");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=invalid_car");

        } catch (ServiceException e) {
            log.error("Admin failed to delete car id={}", carIdParam, e);
            response.sendRedirect(request.getContextPath()
                    + "/admin/cars?error=" + encode(e.getMessage()));
        }
    }

    private void handleBookingConfirm(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            bookingService.confirm(bookingId);

            log.info("Admin confirmed booking id={}", bookingId);
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?success=booking_confirmed");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?error=invalid_booking");

        } catch (ServiceException e) {
            log.error("Admin failed to confirm booking id={}", bookingIdParam, e);
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?error=" + encode(e.getMessage()));
        }
    }

    private void handleBookingComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            bookingService.complete(bookingId);

            log.info("Admin completed booking id={}", bookingId);
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?success=booking_completed");

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?error=invalid_booking");

        } catch (ServiceException e) {
            log.error("Admin failed to complete booking id={}", bookingIdParam, e);
            response.sendRedirect(request.getContextPath()
                    + "/admin/bookings?error=" + encode(e.getMessage()));
        }
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "error";
        }
    }

}
