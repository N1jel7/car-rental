package com.innowise.carrental.web;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.ReviewService;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ReviewServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ReviewServlet.class);

    private ReviewService reviewService;

    @Override
    public void init() {
        reviewService = new ReviewService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/create".equals(pathInfo)) {
            handleCreate(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String bookingIdParam = request.getParameter("bookingId");
        String carIdParam = request.getParameter("carId");
        String ratingParam = request.getParameter("rating");
        String comment = request.getParameter("comment");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            int rating = Integer.parseInt(ratingParam);

            reviewService.create(user.getId(), bookingId, rating, comment);

            log.info("Review created userId={} bookingId={}", user.getId(), bookingId);
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?success=review_created");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=" + ServletUtil.encode(e.getMessage()));

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=" + ServletUtil.encode("Invalid review data"));

        } catch (ServiceException e) {
            log.error("Failed to create review userId={}", user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/cars/" + carIdParam + "?error=" + ServletUtil.encode("Failed to submit review. Please try again"));
        }
    }

}
