package com.company.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 실리콘밸리 엔터프라이즈 MSA 표준 서비스 레지스트리 (Eureka Discovery Server).
 * 마이크로서비스들의 동적 IP/포트 등록 및 Spring Cloud Gateway 로드밸런싱의 원천 정보를 제공합니다.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryApplication.class, args);
    }
}
