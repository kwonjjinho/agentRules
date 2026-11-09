---
description: Spring Security 6.x, JWT Authentication, Role-Based Access Control (RBAC), and API Gateway Security Relay rules.
globs: **/security/**, **/*Security*.java, **/auth/**, **/jwt/**, **/filter/**
alwaysApply: false
---

# Spring Security 6.x & Microservice Security Standards

## 1. Zero-Trust & Stateless Architecture

- **Stateless Session Policy**:
  - Always configure `SessionCreationPolicy.STATELESS`. No HTTP sessions in microservices.
  ```java
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(AbstractHttpConfigurer::disable)
          .cors(Customizer.withDefaults())
          .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
              .requestMatchers("/api/v1/auth/**").permitAll()
              .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
          .build();
  }
  ```
- **CSRF**: Disabled for pure stateless REST APIs using Bearer Tokens.
- **CORS**: Explicit whitelist via `CorsConfigurationSource`; never allow wildcard `*` with `AllowCredentials(true)`.

## 2. JWT & Token Lifecycle Rules

- **Token Structure**:
  - Access Token (short-lived, 15-30 mins): Store user ID (`sub`), tenant, and roles (`authorities`). Do not put sensitive data (passwords, PII) in payload.
  - Refresh Token (longer-lived, 7-14 days): Stored securely in Redis with sliding expiration, bound to device/fingerprint.
- **Token Invalidation / Blacklisting**:
  - Check revoked access tokens against Redis blacklist on logout (`SETEX blacklist:{jti} {ttl} revoked`).
- **Signature & Algorithms**:
  - Use asymmetric cryptography (RS256 / EdDSA) for enterprise MSA:
    - Auth Service / IdP signs tokens with Private Key.
    - Downstream services verify tokens with Public Key (via JWKS endpoint or cached public key).

## 3. Gateway Authentication & Token Relay

- **Gateway Pre-Authentication**:
  - Spring Cloud Gateway validates the JWT signature, extracts claims, and injects trusted internal headers:
    - `X-User-Id`: Authenticated user UUID.
    - `X-User-Roles`: Comma-separated roles (e.g., `ROLE_USER,ROLE_ADMIN`).
  - Drop any incoming external request attempting to spoof `X-User-*` headers at the Gateway boundary.
- **Internal Microservice Trust**:
  - Microservices extract identity from `X-User-*` headers or shared JWT filter.
  - Reject direct external traffic bypassing the Gateway (network isolation / mTLS).

## 4. Fine-Grained Authorization (RBAC / ABAC)

- **Method Security**:
  - Enable `@EnableMethodSecurity(prePostEnabled = true)`.
  - Use declarative annotations on Use Case or Controller methods:
    ```java
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public OrderResponse getOrderById(Long orderId, String userId) { ... }
    ```
- **Principle of Least Privilege**: Grant only minimal necessary roles and permissions.
