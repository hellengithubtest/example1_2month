package org.springsecurity.example.rest;

import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.dto.converters.UserDtoConverter;
import org.springsecurity.example.model.User;
import org.springsecurity.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserDtoConverter userDtoConverter;
    private final UserService userService;

    @GetMapping("/user")
    public List<UserDto> getAll() {
        return userService.getAll()
                .stream()
                .map(userDtoConverter::toDto)
                .toList();
    }

    @GetMapping("/user/{id}")
    public UserDto getById(@PathVariable("id") long userId) {
        User user = userService.getById(userId);
        return userDtoConverter.toDto(user);
    }
}
