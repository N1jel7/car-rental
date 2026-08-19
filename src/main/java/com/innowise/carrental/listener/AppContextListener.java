package com.innowise.carrental.listener;

import com.innowise.carrental.config.ThymeleafConfig;
import com.innowise.carrental.db.ConnectionPool;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);
    public static final String TEMPLATE_ENGINE_ATTRIBUTE = "templateEngine";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ConnectionPool.getInstance();
            log.info("Connection pool initialized");
        } catch (Exception e) {
            log.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Application startup failed", e);
        }

        ServletContext servletContext = servletContextEvent.getServletContext();
        TemplateEngine engine = ThymeleafConfig.buildEngine(servletContext);
        servletContext.setAttribute(TEMPLATE_ENGINE_ATTRIBUTE, engine);
        log.info("Thymeleaf engine initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionPool.getInstance().shutdown();
        log.info("Connection pool shut down");
    }

}
