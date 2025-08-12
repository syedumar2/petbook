package com.petbook.petbook_backend.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;


    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageUrl> images;

    private boolean adopted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private boolean approved = true;

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

}
