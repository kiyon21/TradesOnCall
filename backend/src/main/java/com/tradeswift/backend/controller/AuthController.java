package com.tradeswift.backend.controller;

import com.tradeswift.backend.model.dto.request.LoginRequest;
import com.tradeswift.backend.model.dto.request.RefreshTokenRequest;
import com.tradeswift.backend.model.dto.response.ApiResponse;
import com.tradeswift.backend.model.dto.response.AuthResponse;
import com.tradeswift.backend.model.dto.response.RefreshTokenResponse;
import com.tradeswift.backend.service.user.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user login, logout, and token management")
public class AuthController {
    private final UserAuthService userAuthService;

    @Operation(
            summary = "User Login",
            description = "Authenticate a user with phone number and password. Returns access token and refresh token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or credentials"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userAuthService.login(loginRequest);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login Successful.", authResponse
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh Access Token",
            description = "Refresh an expired access token using a valid refresh token. Returns new access and refresh tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
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

    @Operation(
            summary = "User Logout",
            description = "Logout the current user and invalidate the access token. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logged out successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
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
