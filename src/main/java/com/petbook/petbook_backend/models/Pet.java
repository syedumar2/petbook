package com.petbook.petbook_backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;

    private String breed;
    @Column(nullable = false)
    private String location;

    //Learn this in depth later
    @ElementCollection
    @CollectionTable(name = "pet_images",joinColumns = @JoinColumn(name = "pet_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    private boolean adopted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

}
