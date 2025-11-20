package com.tradeswift.backend.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request DTO for user login")
public class LoginRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "User's phone number in international format (e.g., +1234567890)", example = "+1234567890", required = true)
    private String phone;

    @NotBlank
    @Schema(description = "User's password", example = "SecurePass123!", required = true)
    private String password;
}