package  com.tradeswift.backend.service.user;

import com.tradeswift.backend.exception.BadRequestException;
import com.tradeswift.backend.exception.DuplicateResourceException;
import com.tradeswift.backend.exception.ResourceNotFoundException;
import com.tradeswift.backend.model.dto.request.ChangePasswordRequest;
import com.tradeswift.backend.model.dto.request.LoginRequest;
import com.tradeswift.backend.model.dto.request.RegisterRequest;
import com.tradeswift.backend.model.dto.response.UserResponse;
import com.tradeswift.backend.model.entity.User;
import com.tradeswift.backend.model.enums.UserStatus;
import com.tradeswift.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.tradeswift.backend.service.user.UserService.convertToResponse;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers user in db if not already registered.
     * @param registerRequest new user to register.
     * @return UserResponse
     */
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
        return convertToResponse(savedUser);
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
        return convertToResponse(user);
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