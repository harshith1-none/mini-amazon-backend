package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.AuthenticationResponse;
import com.harshith.mini_amazon_backend.dto.LoginRequest;
import com.harshith.mini_amazon_backend.dto.RegisterRequest;
import com.harshith.mini_amazon_backend.entity.Role;
import com.harshith.mini_amazon_backend.entity.User;
import com.harshith.mini_amazon_backend.exception.EmailAlreadyExistsException;
import com.harshith.mini_amazon_backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Never store the raw password - only the BCrypt hash.
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);
        return new AuthenticationResponse(token, savedUser.getName(), savedUser.getEmail());
    }

    public AuthenticationResponse login(LoginRequest request) {
        // Delegates the actual password check to Spring Security's
        // AuthenticationManager (-> DaoAuthenticationProvider ->
        // PasswordEncoder.matches(...)). Throws BadCredentialsException on
        // failure, which GlobalExceptionHandler turns into a clean 401 -
        // this class never compares passwords itself.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database: " + request.getEmail()));

        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token, user.getName(), user.getEmail());
    }
}
