package com.bohdanzhuvak.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
  private final JwtProperties jwtProperties;

  public boolean validateToken(String token) {
    try {
      return !extractClaim(token, Claims::getExpiration).before(new Date());
    } catch (Exception e) {
      return false;
    }

  }

  public UserDTO getUserFromToken(String token) {
    Claims claims = extractAllClaims(token);
    String userId = claims.getSubject();
    String roles = claims.get("roles", String.class);
    return new UserDTO(userId, roles);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(jwtProperties.getSecretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

}
