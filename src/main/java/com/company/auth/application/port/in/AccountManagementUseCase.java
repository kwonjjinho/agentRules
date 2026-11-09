package com.company.auth.application.port.in;

import com.company.auth.adapter.in.web.dto.ChangePasswordRequest;
import com.company.auth.adapter.in.web.dto.ChangeUsernameRequest;

public interface AccountManagementUseCase {
    void changeUsername(String userId, ChangeUsernameRequest request);
    void changePassword(String userId, ChangePasswordRequest request);
}
