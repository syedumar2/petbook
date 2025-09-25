package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.ImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetImageUrlsRepository extends JpaRepository<ImageUrl,Long> {
    List<ImageUrl> findByPetId(Long petId);
}
