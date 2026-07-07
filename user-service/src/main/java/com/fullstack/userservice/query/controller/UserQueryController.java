package com.fullstack.userservice.query.controller;

import com.fullstack.commonservice.advice.UnauthorizedException;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.user.query.GetUserByEmailQuery;
import com.fullstack.commonservice.user.query.GetUserByIdQuery;
import com.fullstack.commonservice.user.result.UserResult;
import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import com.fullstack.userservice.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserQueryController {
    private final QueryGateway queryGateway;

    @Value("${app.internal-token}")
    private String internalToken;

    @GetMapping("/internal/users/by-email/{email}")
    public ResponseEntity<ResponseData<UserResponse>> getByEmail(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @PathVariable String email) {
        checkInternalToken(token);
        UserResult user = queryGateway.query(new GetUserByEmailQuery(email),
                ResponseTypes.instanceOf(UserResult.class)).join();
        return ResponseEntity.ok(new ResponseData<>("USER_FOUND", "User found", toResponse(user)));
    }

    @GetMapping("/internal/users/{id}")
    public ResponseEntity<ResponseData<UserResponse>> getById(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @PathVariable Long id) {
        checkInternalToken(token);
        UserResult user = queryGateway.query(new GetUserByIdQuery(id),
                ResponseTypes.instanceOf(UserResult.class)).join();
        return ResponseEntity.ok(new ResponseData<>("USER_FOUND", "User found", toResponse(user)));
    }

    private void checkInternalToken(String token) {
        if (!internalToken.equals(token)) {
            throw new UnauthorizedException("Invalid internal token");
        }
    }

    private UserResponse toResponse(UserResult user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(Role.valueOf(user.getRole()))
                .status(UserStatus.valueOf(user.getStatus()))
                .build();
    }
}
