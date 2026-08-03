package com.innowise.carrental.db;

import com.innowise.carrental.exception.DaoException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class ConnectionPool {

    private static final String CONFIG_FILE = "app.properties";
    private static final ConnectionPool INSTANCE = new ConnectionPool();

    private final BlockingQueue<Connection> availableConnections;
    private final List<Connection> allConnections;

    private ConnectionPool() {
        Properties props = loadProperties();

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        int size = Integer.parseInt(props.getProperty("db.pool.size"));

        this.availableConnections = new LinkedBlockingQueue<>(size);
        this.allConnections = new ArrayList<>(size);

        try {
            for (int i = 0; i < size; i++) {
                Connection real = DriverManager.getConnection(url, user, password);
                allConnections.add(real);
                availableConnections.offer(real);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize connection pool", e);
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = ConnectionPool.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + CONFIG_FILE, e);
        }
        return props;
    }

    public static ConnectionPool getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws DaoException {
        try {
            Connection real = availableConnections.take();
            return wrapConnection(real);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DaoException("Interrupted while waiting for a free connection", e);
        }
    }

    private Connection wrapConnection(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        availableConnections.offer(real);
                        return null;
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
        );
    }

    public void shutdown() {
        for (Connection c : allConnections) {
            try {
                c.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
