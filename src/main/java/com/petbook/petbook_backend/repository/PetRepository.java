package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PetRepository extends JpaRepository<Pet,Long>, QueryByExampleExecutor<Pet>, JpaSpecificationExecutor<Pet> {
    List<Pet> findByOwnerId(Long ownerId);
    Optional<Pet> findById(Long id);
    List<Pet> findByApproved(boolean approved);



}
