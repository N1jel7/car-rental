package com.innowise.carrental.util;

import com.innowise.carrental.entity.User;
import com.innowise.carrental.filter.AuthFilter;
import com.innowise.carrental.listener.AppContextListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ServletUtil {

    private ServletUtil() {

    }

    public static int parsePageParam(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return page > 0 ? page : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static WebContext buildWebContext(HttpServletRequest request,
                                             HttpServletResponse response,
                                             ServletContext servletContext) {

        Locale locale = resolveLocale(request);

        return new WebContext(
                JakartaServletWebApplication
                        .buildApplication(servletContext)
                        .buildExchange(request, response),
                locale
        );
    }

    private static Locale resolveLocale(HttpServletRequest request) {
        // 1. Check logged-in user's saved locale
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(AuthFilter.SESSION_USER);
            if (user != null && user.getLocale() != null) {
                return Locale.forLanguageTag(user.getLocale());
            }
            // 2. Check session locale (set by LocaleServlet for guests)
            String sessionLocale = (String) session.getAttribute("locale");
            if (sessionLocale != null) {
                return Locale.forLanguageTag(sessionLocale);
            }
        }
        // 3. Fallback to browser locale
        return request.getLocale();
    }

    public static void render(
            String template,
            WebContext ctx,
            HttpServletResponse response,
            ServletContext servletContext
    ) throws IOException {

        TemplateEngine engine = (TemplateEngine) servletContext.getAttribute(AppContextListener.TEMPLATE_ENGINE_ATTRIBUTE);
        response.setContentType("text/html;charset=UTF-8");
        engine.process(template, ctx, response.getWriter());
    }
}
