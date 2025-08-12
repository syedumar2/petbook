package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetImageUrlsRepository extends JpaRepository<ImageUrl,Long> {
}
