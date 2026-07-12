package com.fullstack.userservice.config;

import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import com.fullstack.userservice.query.model.UserReadModel;
import com.fullstack.userservice.query.projection.UserReadModelRepository;
import com.fullstack.commonservice.user.command.CreateUserCommand;
import java.util.List;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
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
    private final CommandGateway commandGateway;
    private final EventStore eventStore;

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

        List<UserReadModel> users = repository.findAll();
        for (UserReadModel existing : users) {
            String aggregateId = existing.getId().toString();
            if (eventStore.readEvents(aggregateId).hasNext()) {
                continue;
            }
            commandGateway.sendAndWait(new CreateUserCommand(
                    existing.getId(),
                    existing.getEmail(),
                    existing.getUsername(),
                    existing.getFullName(),
                    existing.getPhone(),
                    existing.getRole().name(),
                    existing.getStatus().name()));
        }
    }
}
