package com.fullstack.commonservice.user.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserCommand {
    @TargetAggregateIdentifier
    private Long userId;
}
