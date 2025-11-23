package com.tradesoncall.backend.service.user;

import com.tradesoncall.backend.model.dto.response.UserResponse;
import com.tradesoncall.backend.model.entity.User;
import com.tradesoncall.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Helper method to convert User into Response dto
     * @param user
     * @return UserResponse
     */
    public static UserResponse convertToUserResponse(User user) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .userStatus(user.getStatus())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
