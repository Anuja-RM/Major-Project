package com.Major.majorProject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Double hourlyRate;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private CafeOwner owner;

    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PC> pcs;
}

