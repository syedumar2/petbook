package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.Pet;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PetRepository extends JpaRepository<Pet,Long>, QueryByExampleExecutor<Pet>, JpaSpecificationExecutor<Pet> {

    @EntityGraph(attributePaths = {"images"})
    Page<Pet> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Pet> findById(Long id);
    List<Pet> findByApproved(boolean approved);
    Page<Pet> findByApproved(boolean approved,Pageable pageable);
    Optional<Pet> findByIdAndApproved(Long id,boolean approved);

    List<Pet> findTop10ByNameIgnoreCaseStartingWith(String name);

    // Type autocomplete
    List<Pet> findDistinctTop10ByTypeIgnoreCaseStartingWith(String type);

    // Breed autocomplete
    List<Pet> findDistinctTop10ByBreedIgnoreCaseStartingWith(String breed);

    List<Pet> findDistinctTop10ByLocationIgnoreCaseStartingWith(String location);




}
