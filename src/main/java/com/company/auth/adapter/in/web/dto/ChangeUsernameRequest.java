package com.company.auth.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 아이디(계정명) 변경 요청 DTO")
public record ChangeUsernameRequest(
    @Schema(description = "새로운 아이디", example = "silicon_developer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새로운 아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하이어야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "아이디는 영문, 숫자, 밑줄(_), 하이픈(-)만 사용할 수 있습니다.")
    String newUsername,

    @Schema(description = "본인 인증을 위한 현재 비밀번호", example = "CurrentPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본인 확인을 위해 현재 비밀번호를 입력해야 합니다.")
    String currentPassword
) {}
