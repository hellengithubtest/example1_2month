package org.springsecurity.example.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.dto.converters.UserDtoConverter;
import org.springsecurity.example.model.User;
import org.springsecurity.example.service.UserService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserDtoConverter userDtoConverter;
    private final UserService userService;

/*    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/user")
    public UserDto create(@RequestBody UserDto userDto) {
        var user = userDtoConverter.toDomain(userDto);
        User userSave = userService.create(user);
        return userDtoConverter.toDto(userSave);
    }

    @PutMapping("/user")
    public UserDto update(@RequestBody UserDto userDto) {
        var user = userDtoConverter.toDomain(userDto);
        User userUpdate = userService.update(user);
        return userDtoConverter.toDto(userUpdate);
    }*/
}
