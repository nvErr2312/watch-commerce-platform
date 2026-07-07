package com.fullstack.userservice.dto.request;

import com.fullstack.userservice.command.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {
    @NotNull
    private UserStatus status;
}
