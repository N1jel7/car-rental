package com.innowise.carrental.web;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.filter.AuthFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebServlet("/locale")
public class LocaleServlet extends HttpServlet {

    private static final Set<String> SUPPORTED = Set.of("ru", "en", "be");
    private static final String DEFAULT_LOCALE = "ru";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String locale = request.getParameter("locale");
        String redirect = request.getParameter("redirect");

        if (!SUPPORTED.contains(locale)) {
            locale = DEFAULT_LOCALE;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("locale", locale);

        User user = (User) session.getAttribute(AuthFilter.SESSION_USER);
        if (user != null) {
            user.setLocale(locale);
            session.setAttribute(AuthFilter.SESSION_USER, user);
        }

        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
            redirect = "/cars";
        }

        response.sendRedirect(request.getContextPath() + redirect);
    }

}
