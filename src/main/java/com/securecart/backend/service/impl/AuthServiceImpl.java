package com.securecart.backend.service.impl;

import com.securecart.backend.dto.AuthResponse;
import com.securecart.backend.dto.LoginRequest;
import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.dto.RegisterRequest;
import com.securecart.backend.entity.Cart;
import com.securecart.backend.entity.Role;
import com.securecart.backend.entity.User;
import com.securecart.backend.enums.RoleName;
import com.securecart.backend.exception.BadRequestException;
import com.securecart.backend.exception.UnauthorizedException;
import com.securecart.backend.repository.CartRepository;
import com.securecart.backend.repository.RoleRepository;
import com.securecart.backend.repository.UserRepository;
import com.securecart.backend.security.JwtUtil;
import com.securecart.backend.security.UserPrincipal;
import com.securecart.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use.");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Default USER role is missing."));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);

        Cart cart = Cart.builder()
                .user(savedUser)
                .totalAmount(BigDecimal.ZERO)
                .build();
        cartRepository.save(cart);

        return MessageResponse.builder()
                .message("User registered successfully.")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtUtil.generateToken(principal);
            long expiresAt = System.currentTimeMillis() + jwtUtil.getJwtExpirationMs();

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresAt(expiresAt)
                    .username(principal.getUsername())
                    .roles(principal.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(Collectors.toSet()))
                    .build();
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username or password.");
        }
    }
}
