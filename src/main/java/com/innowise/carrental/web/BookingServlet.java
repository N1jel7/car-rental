package com.innowise.carrental.web;

import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/bookings/*")
public class BookingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BookingServlet.class);
    private static final String BOOKINGS_PAGE = "/WEB-INF/templates/bookings/list.html";
    private static final int PAGE_SIZE = 10;

    private BookingService bookingService;

    @Override
    public void init() {
        bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // GET /bookings or GET /bookings/ — show bookings list
        if (pathInfo == null || pathInfo.equals("/")) {
            showBookingsList(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // POST /bookings/create — create a new booking
    // POST /bookings/cancel — cancel an existing booking
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/create".equals(pathInfo)) {
            handleCreate(request, response);
        } else if ("/cancel".equals(pathInfo)) {
            handleCancel(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showBookingsList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getSessionUser(request);
        int page = parsePageParam(request.getParameter("page"));

        try {
            List<Booking> bookings = bookingService.findByUser(user.getId(), page, PAGE_SIZE);
            int total = bookingService.countByUser(user.getId());
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            request.setAttribute("bookings", bookings);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);

            request.getRequestDispatcher(BOOKINGS_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Failed to load bookings for userId={}", user.getId(), e);
            request.setAttribute("error", "Failed to load bookings. Please try again.");
            request.getRequestDispatcher(BOOKINGS_PAGE).forward(request, response);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);
        String carIdParam = request.getParameter("carId");
        String dateFromParam = request.getParameter("dateFrom");
        String dateToParam = request.getParameter("dateTo");

        try {
            long carId = Long.parseLong(carIdParam);
            LocalDate dateFrom = LocalDate.parse(dateFromParam);
            LocalDate dateTo = LocalDate.parse(dateToParam);

            bookingService.create(user.getId(), carId, dateFrom, dateTo);

            log.info("Booking created userId={} carId={}", user.getId(), carId);

            response.sendRedirect(request.getContextPath()
                    + "/bookings?success=booking_created");

        } catch (DateTimeParseException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=invalid_dates");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=" + encode(e.getMessage()));

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/cars?error=invalid_car");

        } catch (ServiceException e) {
            log.error("Failed to create booking userId={} carId={}", user.getId(), carIdParam, e);
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=booking_failed");
        }
    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);
        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            bookingService.cancel(bookingId, user.getId());

            log.info("Booking cancelled bookingId={} userId={}", bookingId, user.getId());
            response.sendRedirect(request.getContextPath()
                    + "/bookings?success=booking_cancelled");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/bookings?error=" + encode(e.getMessage()));

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/bookings?error=invalid_booking");

        } catch (ServiceException e) {
            log.error("Failed to cancel booking id={} userId={}", bookingIdParam, user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/bookings?error=cancel_failed");
        }
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (User) session.getAttribute(AuthFilter.SESSION_USER);
    }

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

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "error";
        }
    }

}
