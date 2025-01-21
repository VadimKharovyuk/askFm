package com.example.askfm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Проверяем только URL, начинающиеся с /admin/
        if (!request.getRequestURI().startsWith("/admin/")) {
            return true;
        }

        // Разрешаем доступ к страницам логина и регистрации админа
        if (request.getRequestURI().equals("/admin/login") ||
                request.getRequestURI().equals("/admin/register")) {
            return true;
        }

        // Проверяем наличие админа в сессии
        HttpSession session = request.getSession();
        if (session.getAttribute("adminId") == null) {
            response.sendRedirect("/admin/login");
            return false;
        }
        return true;
    }
}