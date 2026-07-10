package com.fullstack.userservice.query.projection;

import com.fullstack.commonservice.user.event.UserCreatedEvent;
import com.fullstack.commonservice.user.event.UserDeletedEvent;
import com.fullstack.commonservice.user.event.UserProfileUpdatedEvent;
import com.fullstack.commonservice.user.event.UserStatusUpdatedEvent;
import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;
import com.fullstack.userservice.query.model.UserReadModel;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProjection {
    private final UserReadModelRepository repository;

    @EventHandler
    public void on(UserCreatedEvent event) {
        String email = event.getEmail().trim().toLowerCase();
        String username = event.getUsername().trim().toLowerCase();
        Optional<UserReadModel> existingByEmail = repository.findByEmail(email);
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(event.getUserId())) {
            repository.delete(existingByEmail.get());
            repository.flush();
        }
        Optional<UserReadModel> existingByUsername = repository.findByUsername(username);
        if (existingByUsername.isPresent() && !existingByUsername.get().getId().equals(event.getUserId())) {
            repository.delete(existingByUsername.get());
            repository.flush();
        }
        UserReadModel user = repository.findById(event.getUserId()).orElseGet(UserReadModel::new);

        user.setId(event.getUserId());
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(event.getFullName());
        user.setPhone(event.getPhone());
        user.setRole(Role.valueOf(event.getRole()));
        user.setStatus(UserStatus.valueOf(event.getStatus()));
        user.setUpdatedAt(Instant.now());
        repository.save(user);
    }

    @EventHandler
    public void on(UserStatusUpdatedEvent event) {
        repository.findById(event.getUserId()).ifPresent(user -> {
            user.setStatus(UserStatus.valueOf(event.getStatus()));
            user.setUpdatedAt(Instant.now());
            repository.save(user);
        });
    }

    @EventHandler
    public void on(UserProfileUpdatedEvent event) {
        repository.findById(event.getUserId()).ifPresent(user -> {
            user.setUsername(event.getUsername());
            user.setFullName(event.getFullName());
            user.setPhone(event.getPhone());
            user.setUpdatedAt(Instant.now());
            repository.save(user);
        });
    }

    @EventHandler
    public void on(UserDeletedEvent event) {
        repository.deleteById(event.getUserId());
    }
}
