package org.springsecurity.example.service;

import org.springsecurity.example.model.User;
import org.springsecurity.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }

    @Transactional(readOnly = true)
    public User getByLogin(String login) {
        return userRepository.fetchByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
    }

    @Transactional
    public User create(String login, String rawPassword, String accessLevel) {
        String normalizedLogin = login == null ? "" : login.trim();
        if (!LOGIN_PATTERN.matcher(normalizedLogin).matches()) {
            throw new IllegalArgumentException(
                    "Логин должен содержать от 3 до 50 латинских букв, цифр или символов . _ -"
            );
        }

        int passwordBytes = rawPassword == null
                ? 0
                : rawPassword.getBytes(StandardCharsets.UTF_8).length;
        if (passwordBytes < 8 || passwordBytes > 72) {
            throw new IllegalArgumentException("Пароль должен занимать от 8 до 72 байт");
        }

        if (userRepository.existsByLoginIgnoreCase(normalizedLogin)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        List<String> roles = switch (normalizeAccessLevel(accessLevel)) {
            case "USER" -> List.of("user");
            case "MANAGER" -> List.of("user", "manager");
            case "ADMIN" -> List.of("admin");
            default -> throw new IllegalArgumentException("Выбрана недопустимая роль");
        };

        User user = new User();
        user.setLogin(normalizedLogin);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(long userId, String currentUsername) {
        User user = getById(userId);
        if (user.getLogin().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("Нельзя удалить собственную активную учётную запись");
        }
        userRepository.delete(user);
    }

    private String normalizeAccessLevel(String accessLevel) {
        return accessLevel == null ? "" : accessLevel.trim().toUpperCase(Locale.ROOT);
    }

}
