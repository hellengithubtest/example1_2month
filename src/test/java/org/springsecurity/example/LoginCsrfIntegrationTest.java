package org.springsecurity.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class LoginCsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageContainsRenderedCsrfField() throws Exception {
        MvcResult result = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("${_csrf.token}"))))
                .andReturn();

        assertEquals(1, StringUtils.countOccurrencesOf(
                result.getResponse().getContentAsString(), "name=\"_csrf\""));
    }

    @Test
    void loginPostWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "user1")
                        .param("password", "password"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginPostWithCsrfAuthenticatesUser() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "user1")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void regularUserSeesOnlyOwnProfile() throws Exception {
        mockMvc.perform(get("/users").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Мой профиль")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1")))
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("#3002"))))
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("#3003"))));
    }

    @Test
    @WithMockUser(username = "user2", roles = {"USER", "MANAGER"})
    void managerSeesAllUsers() throws Exception {
        mockMvc.perform(get("/users").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Пользователи")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("#3001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("#3003")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanSeeAllUsersOnUserPage() throws Exception {
        mockMvc.perform(get("/users").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("#3001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("#3003")));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void regularUserCannotReadAnotherUserById() throws Exception {
        mockMvc.perform(get("/users/3002"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/users/3001"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1")));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void userWithoutManagerRoleReceivesAccessDeniedPage() throws Exception {
        mockMvc.perform(get("/company"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));

        mockMvc.perform(get("/403").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("access-denied"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Доступ запрещён")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user1")));
    }
}
