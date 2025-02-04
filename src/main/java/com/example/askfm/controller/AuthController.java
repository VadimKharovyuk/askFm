package com.example.askfm.controller;

import com.example.askfm.config.CookieService;
import com.example.askfm.dto.UserRegistrationDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final CookieService cookieService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "aut/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "aut/register";
        }

        try {
            userService.registerNewUser(registrationDTO);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "aut/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String registered,
                                @RequestParam(required = false) String username,
                                Model model) {

        // Проверяем был ли передан username
        if (username != null) {
            try {
                // Пытаемся найти пользователя в БД
                User user = userService.findByUsername(username);

                // Если пользователь найден и заблокирован
                if (user.isLocked()) {
                    // Перенаправляем на страницу с информацией о блокировке
                    return "redirect:/account-locked?username=" + username;
                }
            } catch (UsernameNotFoundException e) {
                // Если пользователь не найден, добавляем сообщение об ошибке
                model.addAttribute("error", "Пользователь не найден");
                return "aut/login";
            }
        }

        // Проверяем наличие ошибки аутентификации
        if (error != null) {
            // Если пользователь не найден ранее, показываем общее сообщение об ошибке
            if (!model.containsAttribute("error")) {
                model.addAttribute("error", "Неверные учетные данные");
            }
        }

        // Если это успешная регистрация
        if (registered != null) {
            model.addAttribute("registered", "Регистрация успешна! Войдите в систему.");
        }

        return "aut/login";
    }

    @GetMapping("/account-locked")
    public String showLockedAccountPage(@RequestParam String username,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(username);

            // Проверяем статус блокировки
            if (!user.isLocked()) {
                // Если пользователь не заблокирован, перенаправляем на страницу логина
                return "redirect:/login";
            }

            // Добавляем информацию о блокировке в модель
            model.addAttribute("username", username);
            model.addAttribute("lockReason", user.getLockReason());
            model.addAttribute("lockedAt", user.getLockedAt());

            return "aut/account-locked";

        } catch (UsernameNotFoundException e) {
            // Если пользователь не найден, перенаправляем на логин с сообщением об ошибке
            redirectAttributes.addAttribute("error", "Пользователь не найден");
            return "redirect:/login";
        }
    }

    @PostMapping("/login-success")
    public String loginSuccess(HttpServletResponse response, Authentication authentication) {
        String username = authentication.getName();
        cookieService.addUserCookies(response, username);
        log.info("User {} successfully logged in", username);
        return "redirect:/";
    }

    @GetMapping("/logout-page")
    public String logoutPage(Model model) {
        return "aut/logout";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            new SecurityContextLogoutHandler().logout(request, response, auth);
            cookieService.deleteAllUserCookies(request, response);

            // Уничтожение сессии
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            log.info("User {} logged out successfully", username);
        } else {
            log.warn("No authenticated user found during logout");
        }

        redirectAttributes.addFlashAttribute("message", "Вы успешно вышли из системы");
        return "redirect:/login";
    }
}


