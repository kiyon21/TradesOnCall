package com.tradeswift.backend.controller;

import com.tradeswift.backend.model.dto.request.LoginRequest;
import com.tradeswift.backend.model.dto.request.RefreshTokenRequest;
import com.tradeswift.backend.model.dto.response.ApiResponse;
import com.tradeswift.backend.model.dto.response.AuthResponse;
import com.tradeswift.backend.model.dto.response.RefreshTokenResponse;
import com.tradeswift.backend.service.user.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserAuthService userAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userAuthService.login(loginRequest);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login Successful.", authResponse
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        RefreshTokenResponse tokenResponse = userAuthService.refreshToken(request.getRefreshToken());

        ApiResponse<RefreshTokenResponse> response = ApiResponse.success(
                "Token refreshed successfully",
                tokenResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        // Extract token from "Bearer <token>" header
        String token = authHeader.substring(7);

        userAuthService.logout(token);

        ApiResponse<Void> response = ApiResponse.success(
                "Logged out successfully",
                (Void) null
        );

        return ResponseEntity.ok(response);
    }

}
