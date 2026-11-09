package com.company.auth.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public User(String id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.updatedAt = Instant.now();
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
        this.updatedAt = Instant.now();
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }
}
