package com.fullstack.identityservice.service.impl;

import com.fullstack.identityservice.dto.GoogleUserInfo;
import com.fullstack.identityservice.service.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {
    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierImpl(@Value("${app.google.client-id}") String clientId) {
        this.clientId = clientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(StringUtils.hasText(clientId) ? List.of(clientId) : List.of("missing-google-client-id"))
                .build();
    }

    @Override
    public GoogleUserInfo verify(String idToken) {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID is not configured");
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Google token is invalid");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            Boolean emailVerified = payload.getEmailVerified();
            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    Boolean.TRUE.equals(emailVerified));
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalArgumentException("Google token is invalid", exception);
        }
    }
}
