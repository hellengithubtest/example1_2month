package org.springsecurity.example.dto.converters;

import org.springsecurity.example.dto.UserDto;
import org.springsecurity.example.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter {

    public UserDto toDto(User domain) {
        return new UserDto(domain.getId(), domain.getLogin(), null, domain.getRoles());
    }

}
