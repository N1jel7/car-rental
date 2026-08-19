package com.innowise.carrental.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class PropertiesLoader {

    private PropertiesLoader() {
    }

    public static Properties load(String classpathFileName) {
        try (InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream(classpathFileName)) {
            if (in == null) {
                throw new IllegalStateException(classpathFileName + " not found on classpath");
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + classpathFileName, e);
        }
    }

    public static Properties loadOrEmpty(String classpathFileName) {
        try {
            return load(classpathFileName);
        } catch (IllegalStateException e) {
            return new Properties();
        }
    }

}
