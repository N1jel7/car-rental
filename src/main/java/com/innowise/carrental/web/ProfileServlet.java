package com.innowise.carrental.web;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.service.UserService;
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

public class ProfileServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ProfileServlet.class);
    private static final String PROFILE_PAGE = "profile/profile";

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
        ServletUtil.render(PROFILE_PAGE, context, response, getServletContext());
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
                    + "/profile?error=" + ServletUtil.encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to update profile userId={}", user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=" + ServletUtil.encode("Failed to update profile. Please try again"));
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
                        + "/profile?error=" + ServletUtil.encode("New passwords do not match"));
                return;
            }

            userService.changePassword(user.getId(), oldPassword, newPassword);

            // Force re-login after password change for security.
            request.getSession(false).invalidate();
            response.sendRedirect(request.getContextPath()
                    + "/login?success=password_changed");

        } catch (ValidationException e) {
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=" + ServletUtil.encode(e.getMessage()));

        } catch (ServiceException e) {
            log.error("Failed to change password userId={}", user.getId(), e);
            response.sendRedirect(request.getContextPath()
                    + "/profile?error=" + ServletUtil.encode("Failed to change password. Please try again"));
        }
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (User) session.getAttribute(AuthFilter.SESSION_USER);
    }

}
