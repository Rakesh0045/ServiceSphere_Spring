package com.kce.localservices.service;

import com.kce.localservices.dto.UserDTO;
import com.kce.localservices.entity.User;
import com.kce.localservices.event.AnalyticsEvent;
import com.kce.localservices.repository.UserRepository;
import com.kce.localservices.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        eventPublisher.publishEvent(new AnalyticsEvent(this, "total_users", 1));
        if ("Service Provider".equals(user.getRole())) {
            eventPublisher.publishEvent(new AnalyticsEvent(this, "total_providers", 1));
        }
    }

    public Map<String, Object> login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow();

        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getName(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        // Use DTO — never serialize the raw User entity (exposes password hash)
        response.put("user", new UserDTO(user));
        return response;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateUserProfile(String name, String email) {
        User user = getCurrentUser();
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
    }

    /**
     * Change the current user's password.
     * Validates old password before setting the new one.
     */
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}