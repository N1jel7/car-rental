package com.innowise.carrental.web;

import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.BookingService;
import com.innowise.carrental.util.ParseUtil;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class BookingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BookingServlet.class);
    private static final String BOOKINGS_PAGE = "bookings/list";
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
        int page = ServletUtil.parsePageParam(request.getParameter("page"));

        try {
            List<Booking> bookings = bookingService.findByUser(user.getId(), page, PAGE_SIZE);
            int total = bookingService.countByUser(user.getId());
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("bookings", bookings);
            context.setVariable("currentPage", page);
            context.setVariable("totalPages", totalPages);
            ServletUtil.render(BOOKINGS_PAGE, context, response, getServletContext());

        } catch (ServiceException e) {
            log.error("Failed to load bookings for userId={}", user.getId(), e);

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("error", "Failed to load bookings. Please try again.");
            ServletUtil.render(BOOKINGS_PAGE, context, response, getServletContext());
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);
        String carIdParam = request.getParameter("carId");

        long carId;
        try {
            carId = ParseUtil.parseLong(carIdParam, "Invalid car");
        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars?error=" + ServletUtil.encode(e.getMessage()));
            return;
        }

        try {
            LocalDate dateFrom = ParseUtil.parseDate(
                    request.getParameter("dateFrom"), "Please select valid dates");
            LocalDate dateTo = ParseUtil.parseDate(
                    request.getParameter("dateTo"), "Please select valid dates");

            bookingService.create(user.getId(), carId, dateFrom, dateTo);

            log.info("Booking created userId={} carId={}", user.getId(), carId);

            response.sendRedirect(request.getContextPath()
                    + "/bookings?success=booking_created");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carId + "?error=" + ServletUtil.encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to create booking userId={} carId={}", user.getId(), carId, e);
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carId + "?error=" + ServletUtil.encode("Failed to create the booking. Please try again"));
        }
    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);
        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = ParseUtil.parseLong(bookingIdParam, "Invalid booking");
            bookingService.cancel(bookingId, user.getId());

            log.info("Booking cancelled bookingId={} userId={}", bookingId, user.getId());
            response.sendRedirect(request.getContextPath()
                    + "/bookings?success=booking_cancelled");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/bookings?error=" + ServletUtil.encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to cancel booking id={} userId={}", bookingIdParam, user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/bookings?error=" + ServletUtil.encode("Failed to cancel the booking. Please try again"));
        }
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (User) session.getAttribute(AuthFilter.SESSION_USER);
    }

}
