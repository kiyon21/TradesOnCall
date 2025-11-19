package com.tradeswift.backend.security;

import com.tradeswift.backend.config.JwtConfig;
import com.tradeswift.backend.exception.InvalidTokenException;
import com.tradeswift.backend.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    /**
     * Generate JWT token from User
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getAccessExpirationMs());

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("phone", user.getPhone())
                .claim("email", user.getEmail())
                .claim("userType", user.getUserType().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getAccessSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpirationMs());

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getRefreshSigningKey())
                .compact();
    }

    /**
     * Get user ID from JWT token
     */
    public UUID getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getAccessSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Get expiration from JWT token
     */
    public Date getExpirationDateFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getRefreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /**
     * Validate JWT Access token
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getAccessSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }



    /**
     * Get user ID from REFRESH token and validate it
     */
    public String validateRefreshToken(String token) {
        try {
            Claims claims = getRefreshClaims(token);

            // Verify it's a refresh token
            if (!"refresh".equals(claims.get("type"))) {
                throw new InvalidTokenException("Token is not a refresh token");
            }

            return claims.getSubject();  // Returns user ID
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Refresh token has expired");
        } catch (Exception ex) {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    /**
     * Get claims from token
     */
    private Claims getRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getAccessSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getAccessTokenSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getRefreshSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getAccessTokenSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}