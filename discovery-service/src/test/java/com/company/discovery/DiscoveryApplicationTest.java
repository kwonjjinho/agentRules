package com.company.discovery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DiscoveryApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Eureka Server 컨텍스트가 성공적으로 로드되고 Actuator Health 엔드포인트가 200 OK를 반환한다")
    void contextLoadsAndHealthCheckPasses() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("인증되지 않은 Eureka 대시보드 접근 시 401 Unauthorized 에러가 반환된다")
    void dashboardRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("정상적인 HTTP Basic 자격증명 제공 시 Eureka 대시보드가 정상 응답(200 OK)한다")
    void dashboardAccessibleWithBasicAuth() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("eureka", "eureka1234!")
                .getForEntity("http://localhost:" + port + "/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
