package tn.esprit.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Entity representing an Entreprise (Company).
 * 
 * DevOps Test: This comment was added to trigger a Jenkins rebuild
 * and verify that MySQL data persists across pipeline runs.
 * 
 * @author Maramdrira
 * @since 2026-09-27
 */


@Entity
@Table(name = "entreprises")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String adresse;

    @JsonIgnore
    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    private List<Equipe> equipes = new ArrayList<>();
}
