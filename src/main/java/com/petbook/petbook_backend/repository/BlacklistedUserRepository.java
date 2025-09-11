package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.BlacklistedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedUserRepository extends JpaRepository<BlacklistedUser,Long> {
    boolean existsByUserId(Long userId);

}