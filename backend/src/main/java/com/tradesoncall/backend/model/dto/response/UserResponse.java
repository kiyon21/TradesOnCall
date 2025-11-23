package com.tradesoncall.backend.model.dto.response;

import com.tradesoncall.backend.model.enums.UserStatus;
import com.tradesoncall.backend.model.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Response DTO containing user information")
public class UserResponse {
    @Schema(description = "User's unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
    
    @Schema(description = "Type of user (CUSTOMER or TRADESPERSON)", example = "CUSTOMER")
    private UserType userType;
    
    @Schema(description = "User's account status", example = "ACTIVE")
    private UserStatus userStatus;
    
    @Schema(description = "Whether the user's account is verified", example = "true")
    private Boolean isVerified;
    
    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
