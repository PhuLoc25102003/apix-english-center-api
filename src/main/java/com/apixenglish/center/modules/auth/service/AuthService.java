package com.apixenglish.center.modules.auth.service;

import com.apixenglish.center.modules.auth.dto.request.LoginRequest;
import com.apixenglish.center.modules.auth.dto.request.RefreshTokenRequest;
import com.apixenglish.center.modules.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
}
