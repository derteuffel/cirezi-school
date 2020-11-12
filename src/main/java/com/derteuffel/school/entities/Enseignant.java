package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by user on 24/03/2020.
 */
@Data
@Entity
@Table(name = "enseignant")
@Inheritance(strategy = InheritanceType.JOINED)
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Enseignant implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String prenom;
    private String postnom;
    private String telephone;
    private String email;
    private String whatsapp;
    private ArrayList<String> cour_enseigner= new ArrayList<>();
    private String avatar;


    private ArrayList<Long> sallesIds = new ArrayList<>();
    @ManyToMany
    private Collection<Salle> salles;
    @OneToOne
    private Compte compte;

    @OneToMany(mappedBy = "enseignant")
    private Collection<Salaire> salaires;

}
