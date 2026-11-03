package org.springsecurity.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springsecurity.example.model.User;
import org.springsecurity.example.repository.CompanyRepository;
import org.springsecurity.example.repository.UserRepository;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void regularUserReceivesAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanOpenDashboard() throws Exception {
        mockMvc.perform(get("/admin").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Панель администратора")))
                .andExpect(content().string(containsString("ROLE_ADMIN")))
                .andExpect(content().string(containsString("user1")));
    }

    @Test
    void adminLoginRedirectsToAdminDashboard() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    void adminCanCreateUserWithEncodedPasswordAndManagerRoles() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("login", "created.manager")
                        .param("password", "strong-password")
                        .param("accessLevel", "MANAGER"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        Optional<User> createdUser = userRepository.fetchByLogin("created.manager");
        assertTrue(createdUser.isPresent());
        assertTrue(passwordEncoder.matches("strong-password", createdUser.orElseThrow().getPassword()));
        assertTrue(createdUser.orElseThrow().getRoles().contains("user"));
        assertTrue(createdUser.orElseThrow().getRoles().contains("manager"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    void createUserWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .param("login", "missing.csrf")
                        .param("password", "strong-password")
                        .param("accessLevel", "USER"))
                .andExpect(status().isForbidden());

        assertFalse(userRepository.existsByLoginIgnoreCase("missing.csrf"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    void adminCanDeleteAnotherUserWithoutDeletingCompanies() throws Exception {
        long companyCount = companyRepository.count();

        mockMvc.perform(post("/admin/users/3001/delete").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        userRepository.flush();
        assertFalse(userRepository.existsById(3001L));
        assertEquals(companyCount, companyRepository.count());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    void adminCannotDeleteOwnActiveAccount() throws Exception {
        mockMvc.perform(post("/admin/users/3003/delete").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Нельзя удалить собственную активную учётную запись"
                ));

        assertTrue(userRepository.existsById(3003L));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void regularUserCannotCreateOrDeleteUsers() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("login", "forbidden.user")
                        .param("password", "strong-password")
                        .param("accessLevel", "USER"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users/3002/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
