package com.tradeswift.backend.model.dto.request;

import com.tradeswift.backend.model.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request DTO for user registration")
public class RegisterRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "User's phone number in international format (e.g., +1234567890)", example = "+1234567890", required = true)
    private String phone;

    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User's password (minimum 8 characters)", example = "SecurePass123!", required = true, minLength = 8)
    private String password;

    @NotNull(message = "User Type is required")
    @Schema(description = "Type of user (CUSTOMER or TRADESPERSON)", example = "CUSTOMER", required = true)
    private UserType userType;

}
