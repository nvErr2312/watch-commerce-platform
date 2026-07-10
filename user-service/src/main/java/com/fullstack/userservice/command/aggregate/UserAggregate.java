package com.fullstack.userservice.command.aggregate;

import com.fullstack.commonservice.user.command.CreateUserCommand;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import com.fullstack.commonservice.user.command.UpdateUserProfileCommand;
import com.fullstack.commonservice.user.command.UpdateUserStatusCommand;
import com.fullstack.commonservice.user.event.UserCreatedEvent;
import com.fullstack.commonservice.user.event.UserDeletedEvent;
import com.fullstack.commonservice.user.event.UserProfileUpdatedEvent;
import com.fullstack.commonservice.user.event.UserStatusUpdatedEvent;
import com.fullstack.userservice.command.model.UserStatus;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@NoArgsConstructor
public class UserAggregate {
    @AggregateIdentifier
    private Long userId;

    private UserStatus status;
    private String username;
    private String fullName;
    private String phone;

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        AggregateLifecycle.apply(new UserCreatedEvent(
                command.getUserId(),
                command.getEmail().trim().toLowerCase(),
                command.getUsername().trim().toLowerCase(),
                command.getFullName(),
                command.getPhone(),
                command.getRole(),
                command.getStatus()));
    }

    @CommandHandler
    public void handle(UpdateUserStatusCommand command) {
        UserStatus nextStatus = UserStatus.valueOf(command.getStatus());
        if (status == nextStatus) {
            return;
        }
        AggregateLifecycle.apply(new UserStatusUpdatedEvent(command.getUserId(), command.getStatus()));
    }

    @CommandHandler
    public void handle(UpdateUserProfileCommand command) {
        String nextUsername = normalizeUsername(command.getUsername());
        String nextFullName = trimToNull(command.getFullName());
        String nextPhone = trimToNull(command.getPhone());

        if (nextUsername.equals(username)
                && equalsNullable(nextFullName, fullName)
                && equalsNullable(nextPhone, phone)) {
            return;
        }

        AggregateLifecycle.apply(new UserProfileUpdatedEvent(
                command.getUserId(),
                nextUsername,
                nextFullName,
                nextPhone));
    }

    @CommandHandler
    public void handle(DeleteUserCommand command) {
        AggregateLifecycle.apply(new UserDeletedEvent(command.getUserId()));
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        userId = event.getUserId();
        status = UserStatus.valueOf(event.getStatus());
        username = event.getUsername();
        fullName = event.getFullName();
        phone = event.getPhone();
    }

    @EventSourcingHandler
    public void on(UserStatusUpdatedEvent event) {
        status = UserStatus.valueOf(event.getStatus());
    }

    @EventSourcingHandler
    public void on(UserProfileUpdatedEvent event) {
        username = event.getUsername();
        fullName = event.getFullName();
        phone = event.getPhone();
    }

    @EventSourcingHandler
    public void on(UserDeletedEvent event) {
        AggregateLifecycle.markDeleted();
    }

    private String normalizeUsername(String value) {
        return value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean equalsNullable(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}
