package com.fullstack.commonservice.notification.event;

public record EmailVerificationRequestedEvent(String email, String verificationLink) {
}
