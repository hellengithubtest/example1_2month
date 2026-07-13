package org.springsecurity.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
public class AuthorizationManagersConfig {

    /**
    * Альтернатива UnanimousBased, который требует чтобы ВСЕ сказали ДА!!!!
    */
    @Bean
    public AuthorizationManager<RequestAuthorizationContext> authorizationManagerAll() {
        AuthorityAuthorizationManager<RequestAuthorizationContext> manager = AuthorityAuthorizationManager.hasRole("ADMIN");
        return AuthorizationManagers.allOf(manager);
    }

    /**
     * Альтернатива AffirmativeBased, который требует чтобы хотя бы ОДИН сказал ДА!!!!
     */
    @Bean
    public AuthorizationManager<RequestAuthorizationContext> authorizationManagerAny() {
        AuthorityAuthorizationManager<RequestAuthorizationContext> admin = AuthorityAuthorizationManager.hasRole("ADMIN");
        AuthorityAuthorizationManager<RequestAuthorizationContext> manager = AuthorityAuthorizationManager.hasRole("MANAGER");
        AuthorityAuthorizationManager<RequestAuthorizationContext> user = AuthorityAuthorizationManager.hasRole("USER");
        return AuthorizationManagers.anyOf(
                admin, manager, user
        );
    }
}
