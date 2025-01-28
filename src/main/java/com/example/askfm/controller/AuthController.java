package com.example.askfm.controller;

import com.example.askfm.config.CookieService;
import com.example.askfm.dto.UserRegistrationDTO;
import com.example.askfm.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (registered != null) {
            model.addAttribute("registered", "Регистрация успешна! Войдите в систему.");
        }
        return "aut/login";
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
            cookieService.deleteAllUserCookies(response);
            log.info("User {} logged out successfully", username);
        }

        redirectAttributes.addFlashAttribute("message", "Вы успешно вышли из системы");
        return "redirect:/login";
    }
}


//package com.example.askfm.controller;
//
//import com.example.askfm.dto.UserRegistrationDTO;
//import com.example.askfm.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@Controller
//@RequiredArgsConstructor
//public class AuthController {
//    private final UserService userService;
//
//
//    @GetMapping("/register")
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("user", new UserRegistrationDTO());
//        return "aut/register";
//    }
//
//    @PostMapping("/register")
//    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
//                               BindingResult result) {
//        if (result.hasErrors()) {
//            return "aut/register";
//        }
//
//        try {
//            userService.registerNewUser(registrationDTO);
//            return "redirect:/login";
//        } catch (IllegalArgumentException e) {
//            result.rejectValue("username", "error.user", e.getMessage());
//            return "aut/register";
//        }
//    }
//
//    @GetMapping("/login")
//    public String showLoginForm() {
//        return "aut/login";
//    }
//
//    @GetMapping("/logout-page")
//    public String logoutPage(Model model) {
//        return "aut/logout";
//    }
//}

