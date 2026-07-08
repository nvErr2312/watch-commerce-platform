package com.fullstack.identityservice.service;

import com.fullstack.identityservice.dto.GoogleUserInfo;

public interface GoogleTokenVerifier {
    GoogleUserInfo verify(String idToken);
}
