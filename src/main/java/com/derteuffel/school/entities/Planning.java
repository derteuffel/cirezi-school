package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by user on 10/04/2020.
 */
@Data
@Entity
@Table(name = "planning")
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Planning implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateJour = new Date();
    private String date;
    private String cours;
    private String heureDebut;
    private String heureFin;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hebdo hebdo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Enseignant enseignant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateJour() {
        return dateJour;
    }

    public void setDateJour(Date dateJour) {
        this.dateJour = dateJour;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCours() {
        return cours;
    }

    public void setCours(String cours) {
        this.cours = cours;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public Hebdo getHebdo() {
        return hebdo;
    }

    public void setHebdo(Hebdo hebdo) {
        this.hebdo = hebdo;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }
}
