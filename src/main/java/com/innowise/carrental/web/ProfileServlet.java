package com.innowise.carrental.web;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/profile/*")
public class ProfileServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ProfileServlet.class);
    private static final String PROFILE_PAGE = "/WEB-INF/templates/profile/profile.html";

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(PROFILE_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if ("/update".equals(pathInfo)) {
            handleUpdate(request, response);
        } else if ("/password".equals(pathInfo)) {
            handlePasswordChange(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String locale = request.getParameter("locale");

        try {
            userService.updateProfile(user.getId(), fullName, phone, locale);

            User updated = userService.findById(user.getId());
            request.getSession(false).setAttribute(AuthFilter.SESSION_USER, updated);

            response.sendRedirect(request.getContextPath()
                    + "/profile?success=profile_updated");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=" + encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to update profile userId={}", user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=update_failed");
        }
    }

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = getSessionUser(request);

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            if (!newPassword.equals(confirmPassword)) {
                response.sendRedirect(request.getContextPath()
                        + "/profile?error=passwords_do_not_match");
                return;
            }

            userService.changePassword(user.getId(), oldPassword, newPassword);

            // Force re-login after password change for security.
            request.getSession(false).invalidate();
            response.sendRedirect(request.getContextPath()
                    + "/login?success=password_changed");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=" + encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to change password userId={}", user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=password_change_failed");
        }
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (User) session.getAttribute(AuthFilter.SESSION_USER);
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "error";
        }
    }

}
