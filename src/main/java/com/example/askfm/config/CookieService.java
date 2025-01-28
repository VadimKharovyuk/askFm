package com.example.askfm.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    private static final int TWO_HOURS = 2 * 60 * 60; // 2 часа в секундах

    public Cookie createSessionCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(TWO_HOURS);
        cookie.setHttpOnly(true);  // Защита от XSS
        cookie.setSecure(true);    // Только для HTTPS
        cookie.setPath("/");       // Доступно для всего приложения
        return cookie;
    }

    public void addUserCookies(HttpServletResponse response, String username) {
        // Основная сессионная кука
        Cookie sessionCookie = createSessionCookie("SESSIONID", username);
        response.addCookie(sessionCookie);

        // Кука для пользовательских настроек
        Cookie userPrefs = createSessionCookie("user_prefs", username + "_prefs");
        response.addCookie(userPrefs);
    }

    public void deleteAllUserCookies(HttpServletResponse response) {
        deleteCookies(response, "SESSIONID", "user_prefs");
    }

    private void deleteCookies(HttpServletResponse response, String... cookieNames) {
        for (String cookieName : cookieNames) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }
}