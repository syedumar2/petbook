package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.BlacklistedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistedUserRepository extends JpaRepository<BlacklistedUser,Long> {
    boolean existsByUserId(Long userId);
    Optional<BlacklistedUser> findByUserId(Long userId);

}