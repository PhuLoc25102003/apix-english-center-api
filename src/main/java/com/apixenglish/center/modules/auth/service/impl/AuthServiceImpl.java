package com.apixenglish.center.modules.auth.service.impl;

import com.apixenglish.center.common.exception.UnauthorizedException;
import com.apixenglish.center.modules.auth.dto.request.LoginRequest;
import com.apixenglish.center.modules.auth.dto.request.RefreshTokenRequest;
import com.apixenglish.center.modules.auth.dto.response.LoginResponse;
import com.apixenglish.center.modules.auth.service.AuthService;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.entity.UserStatus;
import com.apixenglish.center.modules.user.repository.UserRepository;
import com.apixenglish.center.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        List<String> permissions = userRepository.findActivePermissionCodesByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                permissions
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
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
