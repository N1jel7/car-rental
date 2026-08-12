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
    public static final String TEMPLATE_ENGINE_ATTR = "templateEngine";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ConnectionPool.getInstance();
            log.info("Connection pool initialized");
        } catch (Exception e) {
            log.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Application startup failed", e);
        }

        ServletContext ctx = sce.getServletContext();
        TemplateEngine engine = ThymeleafConfig.buildEngine(ctx);
        ctx.setAttribute(TEMPLATE_ENGINE_ATTR, engine);
        log.info("Thymeleaf engine initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionPool.getInstance().shutdown();
        log.info("Connection pool shut down");
    }

}
