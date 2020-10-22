package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 24/03/2020.
 */
@Data
@Entity
@Table(name = "salle")
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Salle implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String niveau;
    private String principal;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "salle_enseignant",
            joinColumns = @JoinColumn(
                    name = "salle_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "enseignant_id", referencedColumnName = "id"))
    private Collection<Enseignant> enseignants;

}
