package org.springsecurity.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.Cookie;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "server.ssl.key-store=classpath:test-keystore.p12",
                "server.ssl.key-store-password=test-password"
        }
)
@ActiveProfiles("prod")
class ProductionCookieConfigurationTest {

    @Autowired
    private ServerProperties serverProperties;

    @Test
    void productionProfileEnablesHttpsAndSecureSessionCookie() {
        assertTrue(serverProperties.getSsl().isEnabled());
        assertEquals(Boolean.TRUE, serverProperties.getServlet().getSession().getCookie().getSecure());
        assertEquals(Boolean.TRUE, serverProperties.getServlet().getSession().getCookie().getHttpOnly());
        assertEquals(Cookie.SameSite.LAX,
                serverProperties.getServlet().getSession().getCookie().getSameSite());
    }
}
