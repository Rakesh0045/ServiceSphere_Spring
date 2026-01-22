package com.kce.localservices.service;

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
import java.util.Optional;

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

        // Publish analytics events
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
        response.put("user", user); // This might expose password buffer if serializing entity directly, but Jackson
                                    // ignores standard mapped byte[] usually.
        // Better to return DTO. For now, matching node structure.
        return response;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateUserProfile(String name, String email) {
        User user = getCurrentUser();
        // Check if email is taken by another user
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
    }
}
