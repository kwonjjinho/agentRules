package com.company.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 인증 도메인 사용자 엔티티 (Database-per-Service: auth_db).
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "email", nullable = false, length = 128, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "roles", nullable = false, length = 128)
    private String roles;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public User(String id, String email, String passwordHash, String roles) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        this.id = id;
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.roles = (roles == null || roles.isBlank()) ? "ROLE_USER" : roles;
        this.createdAt = Instant.now();
    }
}
