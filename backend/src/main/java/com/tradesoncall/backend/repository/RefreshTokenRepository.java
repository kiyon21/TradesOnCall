package com.tradesoncall.backend.repository;

import com.tradesoncall.backend.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Boolean existsByToken(String token);
    Optional<RefreshToken> findByToken(String token);
    Boolean existsByUserId(UUID userId);

    Boolean deleteByUserId(UUID userId);
    Boolean deleteByToken(String token);
}
