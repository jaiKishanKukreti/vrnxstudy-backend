package com.vrnxstudy.vrnxstudy_api.service;

import com.vrnxstudy.vrnxstudy_api.entity.Role;
import com.vrnxstudy.vrnxstudy_api.entity.User;
import com.vrnxstudy.vrnxstudy_api.repository.RoleRepository;
import com.vrnxstudy.vrnxstudy_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword, String fullName) {

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ROLE_STUDENT not found in database"));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setStatus("ACTIVE");
        user.getRoles().add(studentRole);

        return userRepository.save(user);
    }
}