package com.fullstack.identityservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private String accountId;
    private Long userId;
    private String email;
    private String username;
    private String status;
}
