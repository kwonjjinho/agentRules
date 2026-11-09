package com.company.auth.adapter.in.web;

import com.company.auth.adapter.in.web.dto.ChangePasswordRequest;
import com.company.auth.adapter.in.web.dto.ChangeUsernameRequest;
import com.company.auth.application.port.in.AccountManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Account Management", description = "회원 계정 정보(아이디/비밀번호) 변경 및 보안 관리 API")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountManagementUseCase accountManagementUseCase;

    @Operation(
        summary = "아이디(계정명) 변경",
        description = "Gateway에서 주입된 X-User-Id 헤더를 기반으로 본인 확인 후 새로운 계정명으로 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "아이디 변경 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 현재 비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 중복 아이디",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/username")
    public ResponseEntity<Void> changeUsername(
        @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody ChangeUsernameRequest request
    ) {
        accountManagementUseCase.changeUsername(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "기존 비밀번호 검증 후 새 비밀번호로 교체하고, Redis에 저장된 기존 활성 세션을 즉시 무효화합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "비밀번호 변경 성공 및 기존 세션 로그아웃 완료"),
        @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 이전 비밀번호와 동일",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
        @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        accountManagementUseCase.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
