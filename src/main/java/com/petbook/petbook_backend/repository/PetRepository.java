package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PetRepository extends JpaRepository<Pet,Long> {
    List<Pet> findByOwnerId(Long ownerId);
    List<Pet> findByBreed(String breed);
    List<Pet> findByType(String type);
    List<Pet> findByName(String name);
    List<Pet> findByLocation(String location);



}
