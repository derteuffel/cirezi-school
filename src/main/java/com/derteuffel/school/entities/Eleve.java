package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by user on 25/03/2020.
 */
@Data
@Entity
@Table(name = "eleve")
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

    @ManyToOne
    private Salle salle;

    @ManyToOne
    private Parent parent;
}
