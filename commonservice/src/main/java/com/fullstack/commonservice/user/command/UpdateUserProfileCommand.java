package com.fullstack.commonservice.user.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileCommand {
    @TargetAggregateIdentifier
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
}
