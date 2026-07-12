package com.fullstack.paymentservice.payos;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PayOsClient {
    private final RestClient.Builder restClientBuilder;

    @Value("${app.payos.api-url}")
    private String apiUrl;
    @Value("${app.payos.client-id}")
    private String clientId;
    @Value("${app.payos.api-key}")
    private String apiKey;
    @Value("${app.payos.checksum-key}")
    private String checksumKey;
    @Value("${app.payos.return-url}")
    private String returnUrl;
    @Value("${app.payos.cancel-url}")
    private String cancelUrl;
    @Value("${app.payment.expire-minutes:15}")
    private long expireMinutes;

    public PayOsPaymentLink createPaymentLink(Long orderId, BigDecimal amount) {
        requireConfig();
        int vnd = amount.setScale(0, RoundingMode.HALF_UP).intValueExact();
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expireMinutes));
        long expiredAt = expiresAt.getEpochSecond();
        String description = ("DH" + orderId).substring(0, Math.min(9, ("DH" + orderId).length()));
        String signature = sign("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s"
                .formatted(vnd, cancelUrl, description, orderId, returnUrl));
        Map<String, Object> body = Map.of(
                "orderCode", orderId,
                "amount", vnd,
                "description", description,
                "expiredAt", expiredAt,
                "cancelUrl", cancelUrl,
                "returnUrl", returnUrl,
                "signature", signature);

        JsonNode response = restClientBuilder.baseUrl(apiUrl).build()
                .post()
                .uri("/v2/payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-client-id", clientId)
                .header("x-api-key", apiKey)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        JsonNode data = response == null ? null : response.path("data");
        if (data == null || data.path("checkoutUrl").isMissingNode()) {
            throw new IllegalStateException("payOS did not return checkoutUrl: " + response);
        }
        return new PayOsPaymentLink(data.path("paymentLinkId").asText(), data.path("checkoutUrl").asText(), expiresAt);
    }

    public boolean validSignature(JsonNode data, String signature) {
        requireConfig();
        return signature != null && signature.equals(sign(flatten(data)));
    }

    private String flatten(JsonNode data) {
        StringBuilder value = new StringBuilder();
        ArrayList<String> names = new ArrayList<>();
        data.fieldNames().forEachRemaining(names::add);
        Collections.sort(names);
        for (String name : names) {
            if (!value.isEmpty()) {
                value.append("&");
            }
            value.append(name).append("=").append(data.get(name).asText());
        }
        return value.toString();
    }

    private void requireConfig() {
        if (clientId.isBlank() || apiKey.isBlank() || checksumKey.isBlank()) {
            throw new IllegalStateException("Missing payOS config");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append("%02x".formatted(b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign payOS payload", e);
        }
    }
}
