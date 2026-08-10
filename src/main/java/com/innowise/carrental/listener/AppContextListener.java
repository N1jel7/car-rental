package com.innowise.carrental.listener;

import com.innowise.carrental.db.ConnectionPool;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            ConnectionPool.getInstance();
            log.info("Connection pool initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Application startup failed", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionPool.getInstance().shutdown();
        log.info("Connection pool shut down");
    }

}
