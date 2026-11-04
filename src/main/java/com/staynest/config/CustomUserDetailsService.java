package com.staynest.config;

import com.staynest.entity.Role;
import com.staynest.entity.User;
import com.staynest.repository.UserRepository;
import com.staynest.service.FirestoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final FirestoreService firestoreService;

    public CustomUserDetailsService(UserRepository userRepository,
                                    @Autowired(required = false) FirestoreService firestoreService) {
        this.userRepository = userRepository;
        this.firestoreService = firestoreService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent()) {
            return UserPrincipal.create(userOpt.get());
        }

        // Fallback: Check Cloud Firestore to restore user profile into active session store
        if (firestoreService != null && firestoreService.isConnected()) {
            try {
                Map<String, Object> firestoreUser = firestoreService.findUserByEmail(normalizedEmail);
                if (firestoreUser != null && !firestoreUser.isEmpty()) {
                    logger.info("Restoring user {} from Cloud Firestore into memory", normalizedEmail);
                    User restored = new User();
                    restored.setEmail(normalizedEmail);
                    restored.setFirstName((String) firestoreUser.getOrDefault("firstName", "Guest"));
                    restored.setLastName((String) firestoreUser.getOrDefault("lastName", "User"));
                    restored.setPassword("{noop}restored_from_firestore");
                    String roleStr = (String) firestoreUser.getOrDefault("role", "ROLE_GUEST");
                    try {
                        restored.setRole(Role.valueOf(roleStr));
                    } catch (Exception e) {
                        restored.setRole(Role.ROLE_GUEST);
                    }
                    restored.setPhone((String) firestoreUser.get("phone"));
                    restored.setAvatarUrl((String) firestoreUser.get("avatarUrl"));
                    restored.setActive(true);

                    User saved = userRepository.save(restored);
                    return UserPrincipal.create(saved);
                }
            } catch (Exception e) {
                logger.debug("Could not lookup user in Firestore: {}", e.getMessage());
            }
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
