package com.tradeswift.backend.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request DTO for changing user password")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "User's current password", example = "OldPass123!", required = true)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character"
    )
    @Schema(
            description = "New password (must contain at least one digit, one lowercase letter, one uppercase letter, and one special character)",
            example = "NewSecurePass123!",
            required = true
    )
    private String newPassword;
}