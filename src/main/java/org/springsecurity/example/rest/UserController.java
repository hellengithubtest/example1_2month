package org.springsecurity.example.rest;

import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.dto.converters.UserDtoConverter;
import org.springsecurity.example.model.User;
import org.springsecurity.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserDtoConverter userDtoConverter;
    private final UserService userService;

    @GetMapping("/user")
    public String getAll(Model model, Authentication authentication) {
        List<UserDto> users = userService.getAll()
                .stream()
                .map(userDtoConverter::toDto)
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("currentUser", authentication.getName());
        return "users";
    }

    @ResponseBody
    @GetMapping("/user/{id}")
    public UserDto getById(@PathVariable("id") long userId) {
        User user = userService.getById(userId);
        return userDtoConverter.toDto(user);
    }
}
