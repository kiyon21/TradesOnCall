package com.tradesoncall.backend.model.dto.response;

import com.tradesoncall.backend.model.enums.UserStatus;
import com.tradesoncall.backend.model.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Response DTO containing authentication tokens and user information")
public class AuthResponse {

    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type (always 'Bearer')", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in milliseconds", example = "86400000")
    private Long expiresIn;

    // User information
    @Schema(description = "User's unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "User's phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "Type of user (CUSTOMER or TRADESPERSON)", example = "CUSTOMER")
    private UserType userType;

    @Schema(description = "User's account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Whether the user's account is verified", example = "true")
    private Boolean isVerified;
}