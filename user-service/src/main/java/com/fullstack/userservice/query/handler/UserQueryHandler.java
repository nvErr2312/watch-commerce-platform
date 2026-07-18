package com.fullstack.userservice.query.handler;

import com.fullstack.commonservice.advice.ResourceNotFoundException;
import com.fullstack.commonservice.user.query.FindAllUsersQuery;
import com.fullstack.commonservice.user.query.GetUserByEmailQuery;
import com.fullstack.commonservice.user.query.GetUserByIdQuery;
import com.fullstack.commonservice.user.result.UserListResult;
import com.fullstack.commonservice.user.result.UserResult;
import com.fullstack.userservice.query.model.UserReadModel;
import com.fullstack.userservice.query.projection.UserReadModelRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserQueryHandler {
    private final UserReadModelRepository repository;

    @QueryHandler
    public UserResult handle(GetUserByIdQuery query) {
        return toResult(repository.findById(query.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @QueryHandler
    public UserResult handle(GetUserByEmailQuery query) {
        return toResult(repository.findByEmail(query.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @QueryHandler
    public UserListResult handle(FindAllUsersQuery query) {
        return new UserListResult(repository.findAll().stream().map(this::toResult).toList());
    }

    private UserResult toResult(UserReadModel user) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus().name());
    }
}
