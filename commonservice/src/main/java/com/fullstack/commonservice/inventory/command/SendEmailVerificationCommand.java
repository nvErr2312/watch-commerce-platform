package com.fullstack.commonservice.inventory.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailVerificationCommand {
    @TargetAggregateIdentifier
    private String email;
    private String verificationLink;
}
