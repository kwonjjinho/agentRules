package com.company.auth.adapter.in.web;

import com.company.auth.adapter.in.web.dto.LoginRequest;
import com.company.auth.adapter.in.web.dto.RefreshTokenRequest;
import com.company.auth.adapter.in.web.dto.RegisterRequest;
import com.company.auth.adapter.in.web.dto.TokenResponse;
import com.company.auth.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "회원가입, 로그인, JWT 토큰 재발급(Rotation), 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일과 비밀번호를 BCrypt로 해싱하여 신규 사용자를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String userId = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", userId, "message", "회원가입이 완료되었습니다."));
    }

    @Operation(summary = "로그인 및 JWT 발급", description = "이메일/비밀번호 인증 후 단기 Access Token과 장기 Refresh Token을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 및 토큰 발급 성공")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "토큰 재발급 (Refresh Token Rotation)", description = "유효한 Refresh Token으로 새 Access/Refresh 토큰 쌍을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "로그아웃 및 토큰 무효화", description = "현재 Access Token을 Redis 블랙리스트에 등록하고 Refresh Token을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 완료")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }
}
