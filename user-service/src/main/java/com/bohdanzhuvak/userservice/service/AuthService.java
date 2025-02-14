package com.bohdanzhuvak.userservice.service;

import com.bohdanzhuvak.userservice.dto.LoginRequest;
import com.bohdanzhuvak.userservice.dto.TokenResponse;
import com.bohdanzhuvak.userservice.dto.UserRequest;
import com.bohdanzhuvak.userservice.dto.UserResponse;
import com.bohdanzhuvak.userservice.exception.InvalidCredentialsException;
import com.bohdanzhuvak.userservice.exception.InvalidTokenException;
import com.bohdanzhuvak.userservice.exception.UserAlreadyExistsException;
import com.bohdanzhuvak.userservice.exception.UserNotFoundException;
import com.bohdanzhuvak.userservice.model.Role;
import com.bohdanzhuvak.userservice.model.User;
import com.bohdanzhuvak.userservice.repository.UserRepository;
import com.bohdanzhuvak.userservice.security.JwtCookieUtil;
import com.bohdanzhuvak.userservice.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final JwtCookieUtil cookieUtil;
  private final UserDetailsService userDetailsService;

  @Transactional
  public UserResponse register(UserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new UserAlreadyExistsException(request.email());
    }

    User user = User.builder()
        .email(request.email())
        .passwordHash(passwordEncoder.encode(request.password()))
        .firstName(request.firstName())
        .lastName(request.lastName())
        .roles(Set.of(Role.ROLE_USER))
        .build();

    user = userRepository.save(user);
    log.info("User registered: {}", user.getEmail());
    return mapToResponse(user);
  }

  public TokenResponse login(LoginRequest request, HttpServletResponse response) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new UserNotFoundException(request.email()));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    String refreshToken = tokenProvider.generateRefreshToken(user);
    cookieUtil.addRefreshTokenCookie(response, refreshToken);

    return tokenProvider.generateAccessToken(user);
  }

  private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .roles(user.getRoles())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public TokenResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieUtil.extractRefreshTokenFromCookie(request);

    if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
      cookieUtil.clearRefreshTokenCookie(response);
      throw new InvalidTokenException("Invalid refresh token");
    }

    String userId = tokenProvider.getUserIdFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

    return tokenProvider.generateAccessToken(userDetails);
  }
}
