package com.fullstack.commonservice.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private Long userId;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String role;
    private String status;
}
