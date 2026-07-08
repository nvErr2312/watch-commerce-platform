package com.fullstack.userservice.dto.request;

import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    private String fullName;
    private String phone;

    @NotNull
    private Role role;

    @NotNull
    private UserStatus status;
}
