package com.fullstack.userservice.query.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import com.fullstack.userservice.command.model.Role;
import com.fullstack.userservice.command.model.UserStatus;

@Getter
@Setter
@Entity
@Table(name = "user_read_models")
public class UserReadModel {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    private String fullName;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private Instant updatedAt;
}
