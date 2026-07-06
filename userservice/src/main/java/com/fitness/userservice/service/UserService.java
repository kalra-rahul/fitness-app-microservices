package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import com.fitness.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail());
            return getUserResponse(existingUser);
        }

        User saveUser = new User();
        saveUser.setEmail(request.getEmail());
        saveUser.setFirstName(request.getFirstName());
        saveUser.setKeycloakId(request.getKeycloakId());
        saveUser.setLastName(request.getLastName());
        saveUser.setPassword(request.getPassword());
        User savedUser = userRepository.save(saveUser);

        UserResponse resUser = new UserResponse();
        resUser.setKeycloakId(savedUser.getKeycloakId());
        resUser.setId(savedUser.getId());
        resUser.setPassword(savedUser.getPassword());
        resUser.setEmail(savedUser.getEmail());
        resUser.setFirstName(savedUser.getFirstName());
        resUser.setLastName(savedUser.getLastName());
        resUser.setCreatedAt(savedUser.getCreatedAt());
        resUser.setUpdatedAt(savedUser.getUpdatedAt());
        return resUser;
    }

    private static UserResponse getUserResponse(User existingUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(existingUser.getId());
        userResponse.setKeycloakId(existingUser.getKeycloakId());
        userResponse.setPassword(existingUser.getPassword());
        userResponse.setEmail(existingUser.getEmail());
        userResponse.setFirstName(existingUser.getFirstName());
        userResponse.setLastName(existingUser.getLastName());
        userResponse.setCreatedAt(existingUser.getCreatedAt());
        userResponse.setUpdatedAt(existingUser.getUpdatedAt());
        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setPassword(user.getPassword());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        return userResponse;
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling user service for {}", userId);
        return userRepository.existsByKeycloakId(userId);
    }
}
