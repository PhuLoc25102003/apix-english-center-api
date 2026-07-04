package com.apixenglish.center.modules.auth.service.impl;

import com.apixenglish.center.modules.auth.dto.request.LoginRequest;
import com.apixenglish.center.modules.auth.dto.request.RefreshTokenRequest;
import com.apixenglish.center.modules.auth.dto.response.LoginResponse;
import com.apixenglish.center.modules.auth.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public LoginResponse login(LoginRequest request) {
        return LoginResponse.builder()
                .accessToken("placeholder-access-token")
                .refreshToken("placeholder-refresh-token")
                .tokenType("Bearer")
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        return LoginResponse.builder()
                .accessToken("placeholder-new-access-token")
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .build();
    }
}
