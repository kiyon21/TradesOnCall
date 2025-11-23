package com.tradesoncall.backend.controller;

import com.tradesoncall.backend.model.dto.request.ChangePasswordRequest;
import com.tradesoncall.backend.model.dto.request.RegisterRequest;
import com.tradesoncall.backend.model.dto.response.ApiResponse;
import com.tradesoncall.backend.model.dto.response.UserResponse;
import com.tradesoncall.backend.service.user.UserAuthService;
import com.tradesoncall.backend.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user registration, retrieval, and password management")
public class UserController {
    private final UserAuthService userAuthService;
    private final UserQueryService userQueryService;

    @Operation(
            summary = "Get User by ID",
            description = "Retrieve user information by user ID. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UUID uuid = UUID.fromString(userId);
        UserResponse userResponse = userQueryService.getUserById(uuid);
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User Retrieved Successfully", userResponse
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get Current User",
            description = "Retrieve information about the currently authenticated user. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current user retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Extract phone from authenticated user
        String phone = userDetails.getUsername();

        // Get user by phone
        UserResponse user = userQueryService.getUserByPhone(phone);

        ApiResponse<UserResponse> response = ApiResponse.success(
                "Current user retrieved successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get All Users",
            description = "Retrieve a list of all users in the system. Requires authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> userResponse = userQueryService.getAllUsers();

        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Users Fetched", userResponse
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Change User Password",
            description = "Change the password for a specific user. Requires authentication and the user must provide current password."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or password does not meet requirements"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(@PathVariable String userId, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        UUID uuid = UUID.fromString(userId);
        UserResponse userResponse = userAuthService.changePassword(uuid, changePasswordRequest);
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User Password Successfully", userResponse
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Register New User",
            description = "Register a new user account. This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User already exists with the provided phone or email"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userAuthService.registerUser(request);

        ApiResponse<UserResponse> response = ApiResponse.success(
                "User Registered Successfully", userResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
