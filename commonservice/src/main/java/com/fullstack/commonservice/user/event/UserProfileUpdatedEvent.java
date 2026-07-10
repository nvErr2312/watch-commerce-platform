package com.fullstack.commonservice.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdatedEvent {
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
}
