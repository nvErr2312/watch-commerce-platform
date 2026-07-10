package com.fullstack.paymentservice.payos;

import java.time.Instant;

public record PayOsPaymentLink(String paymentLinkId, String checkoutUrl, Instant expiresAt) {
}
