package com.fullstack.userservice.config;

import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import com.fullstack.userservice.query.model.UserReadModel;
import com.fullstack.userservice.query.projection.UserReadModelRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserReadModelSeeder implements ApplicationRunner {
    private final UserReadModelRepository repository;

    @Value("${app.admin.user-id}")
    private Long userId;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.full-name}")
    private String fullName;

    @Override
    public void run(ApplicationArguments args) {
        UserReadModel user = repository.findById(userId).orElseGet(UserReadModel::new);
        user.setId(userId);
        user.setEmail(email.trim().toLowerCase());
        user.setUsername(username.trim().toLowerCase());
        user.setFullName(fullName);
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        repository.save(user);
    }
}
