package org.springsecurity.example.service;

import org.springsecurity.example.model.User;
import org.springsecurity.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User update(User user) {
        var userDataBase = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        userDataBase.setLogin(user.getLogin());
        userDataBase.setPassword(user.getPassword());
        userDataBase.setRoles(user.getRoles());
        return userRepository.save(userDataBase);
    }
}
