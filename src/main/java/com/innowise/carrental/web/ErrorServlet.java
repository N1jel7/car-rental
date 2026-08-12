package com.innowise.carrental.web;

import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;

import java.io.IOException;

public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Tomcat puts the actual status code here when forwarding to error page
        Integer statusCode = (Integer) request.getAttribute(
                "jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute(
                "jakarta.servlet.error.message");

        if (statusCode == null) {
            statusCode = 500;
        }

        response.setStatus(statusCode);

        WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
        context.setVariable("statusCode", statusCode);
        context.setVariable("message", message);

        String template = switch (statusCode) {
            case 403 -> "error/403";
            case 404 -> "error/404";
            default  -> "error/500";
        };

        ServletUtil.render(template, context, response, getServletContext());
    }

}
