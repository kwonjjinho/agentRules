package com.company.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Eureka Server 보안 설정.
 * 1. 내부 마이크로서비스 인스턴스 등록, 하트비트, 메타데이터 동기화(/eureka/**)는 신속하고 안전한 통신을 위해 허용합니다.
 * 2. 관리자 대시보드 루트 접근 보호를 위해 HTTP Basic 인증을 적용합니다.
 * 3. K8s / Docker 헬스체크 프로브(/actuator/health/**)는 허용합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/eureka/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
