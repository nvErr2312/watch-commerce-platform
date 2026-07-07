package com.fullstack.commonservice.user.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusCommand {
    @TargetAggregateIdentifier
    private Long userId;
    private String status;
}
