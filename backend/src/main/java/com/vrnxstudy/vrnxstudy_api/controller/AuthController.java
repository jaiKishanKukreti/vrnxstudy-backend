package com.vrnxstudy.vrnxstudy_api.controller;

import com.vrnxstudy.vrnxstudy_api.dto.auth.AuthResponse;
import com.vrnxstudy.vrnxstudy_api.dto.auth.LoginRequest;
import com.vrnxstudy.vrnxstudy_api.dto.auth.RegisterRequest;
import com.vrnxstudy.vrnxstudy_api.dto.auth.RegisterResponse;
import com.vrnxstudy.vrnxstudy_api.entity.Role;
import com.vrnxstudy.vrnxstudy_api.entity.User;
import com.vrnxstudy.vrnxstudy_api.exception.ResourceNotFoundException;
import com.vrnxstudy.vrnxstudy_api.repository.UserRepository;
import com.vrnxstudy.vrnxstudy_api.service.AuthService;
import com.vrnxstudy.vrnxstudy_api.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    //register + login
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    //login
    private final UserRepository userRepository;

//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        User user = authService.register(
                request.email(),
                request.password(),
                request.fullName()
        );

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                );
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        String token = jwtService.generateToken(authentication);

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Username not found"));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        AuthResponse response = new AuthResponse(
                token,
                "Bearer",
                3600L,  // or get from jwtService
                user.getId(),
                user.getEmail(),
                roles
        );

        return ResponseEntity.ok(response);
    }



}