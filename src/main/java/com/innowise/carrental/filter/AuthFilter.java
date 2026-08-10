package com.innowise.carrental.filter;

import com.innowise.carrental.entity.Role;
import com.innowise.carrental.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    public static final String SESSION_USER = "user";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(SESSION_USER) : null;

        String requestUri = httpRequest.getRequestURI();

        if (user == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        if (requestUri.startsWith(httpRequest.getContextPath() + "/admin") && user.getRole() != Role.ADMIN) {

            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied");
            return;
        }

        chain.doFilter(request, response);
    }

}
