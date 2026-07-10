package com.fullstack.identityservice.service;

import com.fullstack.identityservice.dto.google.GoogleUserInfo;

public interface GoogleTokenVerifier {
    GoogleUserInfo verify(String idToken);
}
