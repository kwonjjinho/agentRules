package com.company.auth.application.service;

import com.company.auth.adapter.in.web.dto.ChangePasswordRequest;
import com.company.auth.adapter.in.web.dto.ChangeUsernameRequest;
import com.company.auth.application.port.in.AccountManagementUseCase;
import com.company.auth.domain.entity.OutboxEvent;
import com.company.auth.domain.entity.User;
import com.company.auth.domain.exception.BusinessException;
import com.company.auth.domain.exception.ErrorCode;
import com.company.auth.domain.repository.OutboxEventRepository;
import com.company.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManagementService implements AccountManagementUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    @Transactional
    public void changeUsername(String userId, ChangeUsernameRequest request) {
        User user = findUserById(userId);

        // 1. Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        // 2. Validate duplicate username
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 3. Update domain entity
        String oldUsername = user.getUsername();
        user.updateUsername(request.newUsername());

        // 4. Persist Transactional Outbox event for Kafka audit propagation
        outboxEventRepository.save(new OutboxEvent(
            UUID.randomUUID().toString(),
            "com.company.auth.username-changed",
            user.getId(),
            String.format("{\"userId\":\"%s\",\"oldUsername\":\"%s\",\"newUsername\":\"%s\"}",
                user.getId(), oldUsername, request.newUsername()),
            Instant.now()
        ));

        log.info("Username updated successfully for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // 1. Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        // 2. Prevent password reuse
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        // 3. Hash and update password
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        user.updatePasswordHash(newPasswordHash);

        // 4. Redis: Invalidate existing refresh tokens and active sessions
        invalidateUserSessions(userId);

        // 5. Transactional Outbox security audit event
        outboxEventRepository.save(new OutboxEvent(
            UUID.randomUUID().toString(),
            "com.company.auth.password-changed",
            user.getId(),
            String.format("{\"userId\":\"%s\",\"timestamp\":\"%s\"}", user.getId(), Instant.now()),
            Instant.now()
        ));

        log.info("Password changed and active sessions revoked for user ID: {}", userId);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void invalidateUserSessions(String userId) {
        String refreshTokenKey = "auth-service:prod:refresh-token:" + userId;
        redisTemplate.delete(refreshTokenKey);
    }
}
