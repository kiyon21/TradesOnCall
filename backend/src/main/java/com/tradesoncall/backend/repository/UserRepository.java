package com.tradesoncall.backend.repository;

import com.tradesoncall.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // by email
    Optional<User> findByEmail(String email);
    Boolean  existsByEmail(String email);


    // by phone
    Optional<User> findByPhone(String phone);
    Boolean existsByPhone(String phone);
}
