package com.tradeswift.backend.service.user;

import com.tradeswift.backend.exception.ResourceNotFoundException;
import com.tradeswift.backend.model.dto.response.UserResponse;
import com.tradeswift.backend.model.entity.User;
import com.tradeswift.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tradeswift.backend.service.user.UserService.convertToResponse;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Gets all users.
     * @return list of UserResponse
     */
    public List<UserResponse> getAllUsers(){
        List<UserResponse> responseList = new ArrayList<>();
        for(User user : userRepository.findAll()) {
            responseList.add(convertToResponse(user));
        }
        return responseList;
    }

    /**
     * Get the user by UUID
     * @param id
     * @return UserResponse
     */
    public UserResponse getUserById(UUID id){
        if(userRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("User", "id", id.toString());
        }

        return convertToResponse(userRepository.findById(id).get());
    }

    /**
     * Get the user by phone
     * @param phone
     * @return UserResponse
     */
    public UserResponse getUserByPhone(String phone){
        if(userRepository.findByPhone(phone).isEmpty()) {
            throw new ResourceNotFoundException("User", "phone", phone);
        }

        return convertToResponse(userRepository.findByPhone(phone).get());
    }

    /**
     * Get the user by email
     * @param email
     * @return UserResponse
     */
    public UserResponse getUserByEmail(String email){
        if(userRepository.findByEmail(email).isEmpty()) {
            throw new ResourceNotFoundException("User", "email", email);
        }
        return convertToResponse(userRepository.findByEmail(email).get());
    }

}
