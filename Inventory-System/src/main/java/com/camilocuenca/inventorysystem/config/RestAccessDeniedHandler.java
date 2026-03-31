package com.camilocuenca.inventorysystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    @SuppressWarnings("null")
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = accessDeniedException != null ? accessDeniedException.getMessage() : null;
        if (message == null || message.isBlank()) message = "Access is denied";
        String json = "{\"status\":" + HttpServletResponse.SC_FORBIDDEN + ",\"error\":\"Forbidden\",\"message\":\"" + escapeJson(message) + "\"}";

        response.getWriter().write(json);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
