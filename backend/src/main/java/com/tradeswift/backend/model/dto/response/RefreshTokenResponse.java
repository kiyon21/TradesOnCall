package com.tradeswift.backend.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@Schema(description = "Response DTO containing refreshed authentication tokens")
public class RefreshTokenResponse {

    @Schema(description = "New JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "New JWT refresh token for obtaining future access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type (always 'Bearer')", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in milliseconds", example = "86400000")
    private Long expiresIn;

    // User information
    @Schema(description = "User's unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
}
