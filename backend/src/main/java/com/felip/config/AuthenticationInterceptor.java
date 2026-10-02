package com.felip.config;

import org.springframework.web.servlet.HandlerInterceptor;

import com.felip.controller.AuthController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        var session = request.getSession(false);
        if (session != null && session.getAttribute(AuthController.SESSION_USER) != null) return true;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"Inicia sesión para consultar el inventario.\"}");
        return false;
    }
}
