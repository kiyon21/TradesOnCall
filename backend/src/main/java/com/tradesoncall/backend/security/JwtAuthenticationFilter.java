package com.tradesoncall.backend.security;

import com.tradesoncall.backend.exception.BlacklistedTokenException;
import com.tradesoncall.backend.model.entity.User;
import com.tradesoncall.backend.repository.RefreshTokenRepository;
import com.tradesoncall.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // Extract JWT from request header
            String jwt = extractJwtFromRequest(request);

            // Validate token
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateAccessToken(jwt)) {
                // Get user ID from token
                UUID userId = jwtTokenProvider.getUserIdFromAccessToken(jwt);

                // Load user from database
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Create authentication object
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(user.getPhone())
                        .password(user.getPasswordHash())
                        .authorities(new ArrayList<>())
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if(refreshTokenRepository.existsByToken(token)) {
                throw new BlacklistedTokenException("Token has been logged out and is no longer valid.");
            }
            return token;
        }

        return null;
    }
}