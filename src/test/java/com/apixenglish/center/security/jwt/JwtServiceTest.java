package com.apixenglish.center.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    // Use a secure 256-bit key for test
    private static final String TEST_SECRET = "8db1fa122241cf0ea8b4e723528b9ecf8e06385a4f78326a0b9a9d701cd4df9f";

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpirationMinutes(30);
        
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void shouldGenerateValidTokenAndExtractClaims() {
        String userId = "f8c3de3d-1fea-4d7c-a8b3-1c0b3d6c820f";
        String email = "test@apixenglish.com";
        List<String> permissions = List.of("student:create", "attendance:mark");

        String token = jwtService.generateAccessToken(userId, email, permissions);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(permissions, jwtService.extractPermissions(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid-token-string"));
    }

    @Test
    void shouldFailForExpiredToken() {
        // Set short expiration of -5 minutes to simulate expired token
        jwtProperties.setAccessTokenExpirationMinutes(-5);
        jwtService = new JwtService(jwtProperties);

        String token = jwtService.generateAccessToken(
                "f8c3de3d-1fea-4d7c-a8b3-1c0b3d6c820f",
                "test@apixenglish.com",
                List.of("student:create")
        );

        assertFalse(jwtService.isTokenValid(token));
    }
}
