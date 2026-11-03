package org.springsecurity.example.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.dto.converters.UserDtoConverter;
import org.springsecurity.example.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserDtoConverter userDtoConverter;
    private final UserService userService;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        List<UserDto> users = userService.getAll()
                .stream()
                .map(userDtoConverter::toDto)
                .toList();

        long adminCount = users.stream()
                .filter(user -> user.getRoles().contains("admin"))
                .count();

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("currentUser", authentication.getName());
        return "admin";
    }

    @PostMapping("/users")
    public String createUser(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String accessLevel,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.create(login, password, accessLevel);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пользователь «" + login.trim() + "» создан"
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable("id") long userId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.delete(userId, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь удалён");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin";
    }
}
