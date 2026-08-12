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

public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final String LOGIN_PAGE = "auth/login";

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

        WebContext ctx = ServletUtil.buildWebContext(request, response, getServletContext());
        ServletUtil.render("auth/login", ctx, response, getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = userService.login(email, password);

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);
            response.sendRedirect(request.getContextPath() + "/cars");

        } catch (ValidationException e) {
            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("error", e.getMessage());
            context.setVariable("email", email);
            ServletUtil.render(LOGIN_PAGE, context, response, getServletContext());

        } catch (ServiceException e) {
            log.error("Login error email={}", email, e);
            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("error", "Something went wrong. Please try again.");
            ServletUtil.render(LOGIN_PAGE, context, response, getServletContext());
        }
    }

}
