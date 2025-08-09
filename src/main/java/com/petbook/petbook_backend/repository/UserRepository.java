package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.Role;
import com.petbook.petbook_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

}
