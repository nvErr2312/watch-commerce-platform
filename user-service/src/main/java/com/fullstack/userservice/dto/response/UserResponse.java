package com.fullstack.userservice.dto.response;

import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private Role role;
    private UserStatus status;
}
