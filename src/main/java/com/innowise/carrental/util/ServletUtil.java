package com.innowise.carrental.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
}
