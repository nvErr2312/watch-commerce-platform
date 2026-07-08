package com.fullstack.commonservice.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdatedEvent {
    private Long userId;
    private String status;
}
