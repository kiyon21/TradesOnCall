package com.tradeswift.backend.service.user;

import com.tradeswift.backend.config.JwtConfig;
import com.tradeswift.backend.exception.*;
import com.tradeswift.backend.model.dto.request.ChangePasswordRequest;
import com.tradeswift.backend.model.dto.request.LoginRequest;
import com.tradeswift.backend.model.dto.request.RegisterRequest;
import com.tradeswift.backend.model.dto.response.AuthResponse;
import com.tradeswift.backend.model.dto.response.RefreshTokenResponse;
import com.tradeswift.backend.model.dto.response.UserResponse;
import com.tradeswift.backend.model.entity.RefreshToken;
import com.tradeswift.backend.model.entity.User;
import com.tradeswift.backend.model.enums.UserStatus;
import com.tradeswift.backend.repository.RefreshTokenRepository;
import com.tradeswift.backend.repository.UserRepository;
import com.tradeswift.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.tradeswift.backend.service.user.UserService.convertToUserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;


    /**
     * Registers user in db if not already registered.
     * @param registerRequest new user to register.
     * @return UserResponse
     */
    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        // Check if email or phone number exists
        if(userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new DuplicateResourceException("User", "phone",  registerRequest.getPhone());
        }
        if(registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new  DuplicateResourceException("User", "email",  registerRequest.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = User.builder()
                .phone(registerRequest.getPhone())
                .email(registerRequest.getEmail())
                .passwordHash(hashedPassword)
                .userType(registerRequest.getUserType())
                .isVerified(false)
                .status(UserStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by phone
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", request.getPhone()));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        // Check if user is verified
        if (!user.getIsVerified()) {
            throw new BadRequestException("Phone number not verified. Please verify your phone number first.");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        // Delete old refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // Save new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .userId(user.getUserId())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getRefreshExpirationMs())))
                .build();
        refreshTokenRepository.save(refreshToken);

        // Build and return response
        return buildAuthResponse(user, accessToken, refreshTokenString);
    }

    /**
     * Logs user out by revoking their refresh tokens
     */
    @Transactional
    public void logout(String accessToken) {
        try {
            // Extract user ID from access token
            UUID userId = jwtTokenProvider.getUserIdFromAccessToken(accessToken);

            // Revoke all refresh tokens for this user
            if (refreshTokenRepository.existsByUserId(userId)) {
                refreshTokenRepository.deleteByUserId(userId);
                log.info("User {} logged out successfully", userId);
            }
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new InvalidTokenException("Invalid access token");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        // 1. Validate refresh token exists in database
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenString)
                .orElseThrow(() -> new BlacklistedTokenException(
                        "Refresh token is invalid or expired. Please log in again."));

        // 2. Validate refresh token signature and get user ID
        String userIdString = jwtTokenProvider.validateRefreshToken(refreshTokenString);
        UUID userId = UUID.fromString(userIdString);

        // 3. Get user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdString));

        // 4. Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        // 5. Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        // 6. Save new refresh token
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenString)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtConfig.getRefreshExpirationMs())))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("Tokens refreshed for user: {}", userId);

        // 7. Return response
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessExpirationMs() / 1000)
                .userId(userId)
                .build();
    }

    /**
     * Build AuthResponse with both access and refresh tokens
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)  // Add this field to AuthResponse
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessExpirationMs() / 1000)
                .userId(user.getUserId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .userType(user.getUserType())
                .status(user.getStatus())
                .isVerified(user.getIsVerified())
                .build();
    }

    /**
     * Changes password for specific uuid
     * @param uuid
     * @param changePasswordRequest
     * @return
     */
    public UserResponse changePassword(UUID uuid, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", uuid.toString()));

        if(!verifyPassword(changePasswordRequest.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        return convertToUserResponse(user);
    }

    /**
     * Verifies if a raw password matches the stored hash
     *
     * @param rawPassword The plain text password from login
     * @param hashedPassword The BCrypt hash from database
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}