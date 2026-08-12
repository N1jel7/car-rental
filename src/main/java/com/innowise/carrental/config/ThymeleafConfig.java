package com.innowise.carrental.config;

import jakarta.servlet.ServletContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class ThymeleafConfig {
    private ThymeleafConfig() {}

    public static TemplateEngine buildEngine(ServletContext servletContext) {
        JakartaServletWebApplication application =
                JakartaServletWebApplication.buildApplication(servletContext);

        WebApplicationTemplateResolver resolver =
                new WebApplicationTemplateResolver(application);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        // Message resolver — reads from messages_ru.properties etc. in classpath
        ClassLoaderMessageResolver messageResolver = new ClassLoaderMessageResolver();

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setMessageResolver(messageResolver);
        return engine;
    }
}
