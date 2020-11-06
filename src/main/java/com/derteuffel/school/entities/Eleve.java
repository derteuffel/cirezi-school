package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by user on 25/03/2020.
 */
@Data
@Entity
@Table(name = "eleve")
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Eleve implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String prenom;
    private String postnom;
    private String telephoneTuteur;
    private String nomCompletTuteur;
    private String emailTuteur;
    private String whatsappTuteur;
    private int age;
    private String localisation;
    private String autreInformation;
    private String pays;
    private String categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    private Parent parent;

}
