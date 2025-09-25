package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicListingDTO;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.models.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PetRepository extends JpaRepository<Pet, Long>, QueryByExampleExecutor<Pet>, JpaSpecificationExecutor<Pet> {

    @EntityGraph(attributePaths = {"images", "owner"})
    Page<Pet> findAll(Specification<Pet> spec, Pageable pageable);

    @Query("""
            SELECT new com.petbook.petbook_backend.dto.response.PetInfoPublicListingDTO(
                p.id,
                p.name,
                p.type,
                p.breed,
                p.location,
                CONCAT('', p.gender),
                (SELECT i.url
                FROM ImageUrl i
                WHERE i.pet.id = p.id
                AND i.id=(
                SELECT MIN(i2.id) FROM ImageUrl i2 WHERE i2.pet.id = p.id
                )),
                p.adopted,
                u.email,
                p.description,
                u.id,
                p.createdAt
            )
            FROM Pet p
            JOIN p.owner u
            WHERE p.approved = true
            
            """)
    Page<PetInfoPublicListingDTO> findAllProjected(Specification<Pet> spec, Pageable pageable);


    @EntityGraph(attributePaths = {"images", "owner"})
    Page<Pet> findAll(Pageable pageable);


    @EntityGraph(attributePaths = {"images", "owner"})
    Optional<Pet> findById(Long id);

    @Query("""
       SELECT new com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse(
           p.id,
           p.name,
           p.type,
           p.breed,
           p.location,
           CONCAT('', p.gender),
           null,
           p.adopted,
           u.email,
           u.id,
           p.description,
           p.approved,
           p.approvedAt,
           p.rejectedAt,
           p.createdAt
       )
       FROM Pet p
       JOIN p.owner u
       WHERE p.id = :id
       """)
    Optional<PetInfoPrivateResponse> findUserPetById(Long id);

    @Query("""
       SELECT new com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse(
           p.id,
           p.name,
           p.type,
           p.breed,
           p.location,
           CONCAT('', p.gender),
           null,
           p.adopted,
           u.email,
           u.id,
           p.description,
           p.approved,
           p.approvedAt,
           p.rejectedAt,
           p.createdAt
       )
       FROM Pet p
       JOIN p.owner u
       WHERE u.id = :ownerId
       """)
    Page<PetInfoPrivateResponse> findByOwnerIdProjected(@Param("ownerId") Long ownerId, Pageable pageable);


    @EntityGraph(attributePaths = {"images", "owner"})
    Page<Pet> findByApproved(boolean approved, Pageable pageable);


    @Query(
            """
                     SELECT new com.petbook.petbook_backend.dto.response.PetInfoPublicResponse(
                     p.id,
                     p.name,
                     p.type,
                     p.breed,
                     p.location,
                     CONCAT('', p.gender),
                     null,
                     p.adopted,
                     u.email,
                     p.description,
                     u.id,
                     p.createdAt
                     ) FROM Pet p
                     JOIN p.owner u
                     WHERE p.id = :id AND p.approved = :approved
                    """

    )
    Optional<PetInfoPublicResponse> findByIdAndApproved(Long id, boolean approved);

    List<Pet> findTop10ByNameIgnoreCaseStartingWith(String name);

    // Type autocomplete
    List<Pet> findDistinctTop10ByTypeIgnoreCaseStartingWith(String type);

    // Breed autocomplete
    List<Pet> findDistinctTop10ByBreedIgnoreCaseStartingWith(String breed);

    List<Pet> findDistinctTop10ByLocationIgnoreCaseStartingWith(String location);


}
