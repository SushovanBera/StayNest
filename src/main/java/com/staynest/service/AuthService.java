package com.staynest.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.staynest.config.JwtTokenProvider;
import com.staynest.config.UserPrincipal;
import com.staynest.dto.AuthRequest;
import com.staynest.dto.AuthResponse;
import com.staynest.dto.RegisterRequest;
import com.staynest.dto.UserProfileDto;
import com.staynest.entity.Role;
import com.staynest.entity.User;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final FirestoreService firestoreService;
    private final FirebaseAuth firebaseAuth;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       FirestoreService firestoreService,
                       @Autowired(required = false) FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.firestoreService = firestoreService;
        this.firebaseAuth = firebaseAuth;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_GUEST);
        user.setAvatarUrl(request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()
                ? request.getAvatarUrl()
                : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80");
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // 1. Store user in Firebase Firestore
        firestoreService.recordUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole().name(),
                savedUser.getPhone(),
                savedUser.getAvatarUrl()
        );

        // 2. Also register in Firebase Auth if available
        if (firebaseAuth != null) {
            try {
                UserRecord.CreateRequest createReq = new UserRecord.CreateRequest()
                        .setEmail(savedUser.getEmail())
                        .setPassword(request.getPassword())
                        .setDisplayName(savedUser.getFirstName() + " " + savedUser.getLastName())
                        .setDisabled(false);
                firebaseAuth.createUser(createReq);
                logger.info("Created user {} in Firebase Auth console", savedUser.getEmail());
            } catch (Exception e) {
                logger.warn("Could not create Firebase Auth user (may already exist or Auth not enabled): {}", e.getMessage());
            }
        }

        // Auto authenticate on register
        String token = tokenProvider.generateTokenFromUsername(savedUser.getEmail(), savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getAvatarUrl()
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new BadRequestException("Your account has been deactivated. Please contact support.");
        }

        String token = tokenProvider.generateToken(authentication);

        // Store login details in Firebase Firestore (records lastLoginAt and login_history audit log)
        firestoreService.recordLogin(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "127.0.0.1",
                true
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + principal.getId()));

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    @Transactional
    public UserProfileDto updateProfile(UserPrincipal principal, UserProfileDto dto) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName().trim());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName().trim());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().trim());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio().trim());
        }
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(dto.getAvatarUrl().trim());
        }

        User updated = userRepository.save(user);

        // Sync profile changes to Firestore
        firestoreService.recordUser(
                updated.getId(),
                updated.getEmail(),
                updated.getFirstName(),
                updated.getLastName(),
                updated.getRole().name(),
                updated.getPhone(),
                updated.getAvatarUrl()
        );

        return getCurrentUserProfile(UserPrincipal.create(updated));
    }

    @Transactional
    public AuthResponse switchRole(UserPrincipal principal, Role newRole) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only allow switching between GUEST and HOST (ADMIN stays ADMIN)
        if (user.getRole() != Role.ROLE_ADMIN) {
            user.setRole(newRole);
            user = userRepository.save(user);

            // Sync updated role to Firestore
            firestoreService.recordUser(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name(),
                    user.getPhone(),
                    user.getAvatarUrl()
            );
        }

        String token = tokenProvider.generateTokenFromUsername(user.getEmail(), user.getId());
        return new AuthResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }
}
