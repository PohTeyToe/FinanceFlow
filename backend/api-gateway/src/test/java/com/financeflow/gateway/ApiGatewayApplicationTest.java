package com.financeflow.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=",
    "spring.datasource.username=",
    "spring.datasource.password="
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
    }
}
