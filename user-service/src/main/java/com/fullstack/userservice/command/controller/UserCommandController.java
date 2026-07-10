package com.fullstack.userservice.command.controller;

import com.fullstack.commonservice.advice.UnauthorizedException;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.user.command.CreateUserCommand;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import com.fullstack.commonservice.security.jwt.JwtClaims;
import com.fullstack.commonservice.user.command.UpdateUserProfileCommand;
import com.fullstack.commonservice.user.command.UpdateUserStatusCommand;
import com.fullstack.userservice.dto.request.CreateUserRequest;
import com.fullstack.userservice.dto.request.UpdateProfileRequest;
import com.fullstack.userservice.dto.request.UpdateUserStatusRequest;
import com.fullstack.userservice.dto.response.UserResponse;
import com.fullstack.userservice.query.model.UserReadModel;
import com.fullstack.userservice.query.projection.UserReadModelRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserCommandController {
    private final CommandGateway commandGateway;
    private final UserReadModelRepository repository;

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

    @PutMapping("/users/me")
    public ResponseEntity<ResponseData<UserResponse>> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        Long userId = Long.parseLong(claims.userId());
        String username = request.getUsername().trim().toLowerCase();
        String fullName = trimToNull(request.getFullName());
        String phone = trimToNull(request.getPhone());

        repository.findByUsername(username)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Tên người dùng đã được sử dụng");
                });

        log.info("update profile, aggregateId={}", userId);
        commandGateway.sendAndWait(new UpdateUserProfileCommand(
                userId,
                username,
                fullName,
                phone));

        UserReadModel user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        return ResponseEntity.ok(new ResponseData<>("USER_PROFILE_UPDATED", "User profile updated", UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(username)
                .fullName(fullName)
                .phone(phone)
                .role(user.getRole())
                .status(user.getStatus())
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

    private UserResponse toResponse(UserReadModel user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
