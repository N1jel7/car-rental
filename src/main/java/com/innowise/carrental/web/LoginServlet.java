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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final String LOGIN_PAGE = "/WEB-INF/templates/auth/login.html";

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    // just show the login form
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in redirect to catalog
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/cars");
            return;
        }

        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }

    // process the form
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = userService.login(email, password);

            // Create a new session — invalidate old one
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);

            log.info("User logged in email={}", email);

            response.sendRedirect(request.getContextPath() + "/cars");

        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Login service error email={}", email, e);
            request.setAttribute("error", "Something went wrong. Please try again.");
            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
        }
    }

}
