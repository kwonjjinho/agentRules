package com.company.auth.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 비밀번호 변경 요청 DTO")
public record ChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "CurrentPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
    String currentPassword,

    @Schema(description = "새로운 비밀번호 (영문 대소문자, 숫자, 특수문자 조합 8~32자)", example = "NewSecurePass2026!@", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새로운 비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$",
        message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 최소 1개씩 포함해야 합니다."
    )
    String newPassword
) {}
