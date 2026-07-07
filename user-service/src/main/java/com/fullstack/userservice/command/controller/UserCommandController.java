package com.fullstack.userservice.command.controller;

import com.fullstack.commonservice.advice.UnauthorizedException;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.user.command.CreateUserCommand;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import com.fullstack.commonservice.user.command.UpdateUserStatusCommand;
import com.fullstack.userservice.dto.request.CreateUserRequest;
import com.fullstack.userservice.dto.request.UpdateUserStatusRequest;
import com.fullstack.userservice.dto.response.UserResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCommandController {
    private final CommandGateway commandGateway;

    @Value("${app.internal-token}")
    private String internalToken;

    @PostMapping("/internal/users")
    public ResponseEntity<ResponseData<UserResponse>> create(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Valid @RequestBody CreateUserRequest request) {
        checkInternalToken(token);
        Long userId = newUserId();
        commandGateway.sendAndWait(new CreateUserCommand(
                userId,
                request.getEmail(),
                request.getUsername(),
                request.getFullName(),
                request.getPhone(),
                request.getRole().name(),
                request.getStatus().name()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>("USER_CREATED", "User created", UserResponse.builder()
                        .id(userId)
                        .email(request.getEmail().trim().toLowerCase())
                        .username(request.getUsername().trim().toLowerCase())
                        .fullName(request.getFullName())
                        .phone(request.getPhone())
                        .role(request.getRole())
                        .status(request.getStatus())
                        .build()));
    }

    @PatchMapping("/internal/users/{id}/status")
    public ResponseEntity<ResponseData<UserResponse>> updateStatus(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        checkInternalToken(token);
        commandGateway.sendAndWait(new UpdateUserStatusCommand(id, request.getStatus().name()));
        return ResponseEntity.ok(new ResponseData<>("USER_STATUS_UPDATED", "User status updated", UserResponse.builder()
                .id(id)
                .status(request.getStatus())
                .build()));
    }

    @DeleteMapping("/internal/users/{id}")
    public ResponseEntity<ResponseData<Void>> delete(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @PathVariable Long id) {
        checkInternalToken(token);
        commandGateway.sendAndWait(new DeleteUserCommand(id));
        return ResponseEntity.ok(new ResponseData<>("USER_DELETED", "User deleted", null));
    }

    private void checkInternalToken(String token) {
        if (!internalToken.equals(token)) {
            throw new UnauthorizedException("Invalid internal token");
        }
    }

    private Long newUserId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

}
