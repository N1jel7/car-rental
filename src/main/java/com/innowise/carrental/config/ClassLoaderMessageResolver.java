package com.innowise.carrental.config;

import com.innowise.carrental.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.IMessageResolver;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ClassLoaderMessageResolver implements IMessageResolver {

    private static final Logger log = LoggerFactory.getLogger(ClassLoaderMessageResolver.class);

    private static final String BASE_NAME = "messages";
    private static final String UNDERSCORE = "_";
    private static final String DOT_PROPERTIES = ".properties";
    private static final String DEFAULT_LOCALE = "ru";

    private final Map<String, Properties> cache = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "ClassLoaderMessageResolver";
    }

    @Override
    public Integer getOrder() {
        return 1;
    }

    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin,
                                 String key, Object[] messageParameters) {
        Locale locale = context.getLocale();
        Properties props = loadProperties(locale.getLanguage());

        String value = props.getProperty(key);
        if (value == null) {
            props = loadProperties(DEFAULT_LOCALE);
            value = props.getProperty(key);
        }
        if (value == null) {
            log.warn("Missing i18n key: {} for locale: {}", key, locale.getLanguage());
            return "[" + key + "]";
        }

        if (messageParameters != null && messageParameters.length > 0) {
            for (int i = 0; i < messageParameters.length; i++) {
                value = value.replace("{" + i + "}", String.valueOf(messageParameters[i]));
            }
        }

        return value;
    }

    @Override
    public String createAbsentMessageRepresentation(ITemplateContext context,
                                                    Class<?> origin, String key,
                                                    Object[] messageParameters) {
        return "[" + key + "]";
    }

    private Properties loadProperties(String language) {
        return cache.computeIfAbsent(language, l -> {
            String filename = BASE_NAME + UNDERSCORE + l + DOT_PROPERTIES;
            Properties props = PropertiesLoader.loadOrEmpty(filename);
            if (props.isEmpty()) {
                log.warn("Could not load {}", filename);
            }
            return props;
        });
    }

}
