package org.springsecurity.example.rest;

import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.dto.converters.UserDtoConverter;
import org.springsecurity.example.model.User;
import org.springsecurity.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserDtoConverter userDtoConverter;
    private final UserService userService;

    @GetMapping
    public String getAll(Model model, Authentication authentication) {
        boolean canViewAll = hasAnyRole(authentication, "ROLE_MANAGER", "ROLE_ADMIN");
        List<User> visibleUsers = canViewAll
                ? userService.getAll()
                : List.of(userService.getByLogin(authentication.getName()));

        List<UserDto> users = visibleUsers
                .stream()
                .map(userDtoConverter::toDto)
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("canViewAll", canViewAll);
        return "users";
    }

    @ResponseBody
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable("id") long userId, Authentication authentication) {
        User user = userService.getById(userId);
        boolean canViewAll = hasAnyRole(authentication, "ROLE_MANAGER", "ROLE_ADMIN");
        if (!canViewAll && !user.getLogin().equals(authentication.getName())) {
            throw new AccessDeniedException("A user can only view their own profile");
        }
        return userDtoConverter.toDto(user);
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> List.of(roles).contains(authority.getAuthority()));
    }
}
