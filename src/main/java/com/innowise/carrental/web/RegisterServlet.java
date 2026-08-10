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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RegisterServlet.class);
    private static final String REGISTER_PAGE = "/WEB-INF/templates/auth/register.html";

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/cars");
            return;
        }

        request.getRequestDispatcher(REGISTER_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        try {
            User user = userService.register(email, password, fullName, phone);

            // Log in immediately after registration — no need to make
            // the user fill in the login form right after signing up.
            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);

            log.info("Registered and logged in user email={}", email);
            response.sendRedirect(request.getContextPath() + "/cars");

        } catch (ValidationException e) {
            // Send form values back so the user doesn't have to retype everything
            request.setAttribute("error", e.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher(REGISTER_PAGE).forward(request, response);

        } catch (ServiceException e) {
            log.error("Registration service error email={}", email, e);
            request.setAttribute("error", "Something went wrong. Please try again.");
            request.getRequestDispatcher(REGISTER_PAGE).forward(request, response);
        }
    }

}
