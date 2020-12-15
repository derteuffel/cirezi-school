package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * Created by user on 10/04/2020.
 */
@Entity
@Data
@Table(name = "hebdo")
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Hebdo implements Serializable {


    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date debut;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fin;

    private String filePath;

    @ManyToMany(fetch = FetchType.LAZY)
    private Collection<Salle> salles;

    @ManyToOne(fetch = FetchType.LAZY)
    private Compte compte;


    public Hebdo() {
    }

    public Hebdo(Date debut, Date fin, String filePath) {
        this.debut = debut;
        this.fin = fin;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDebut() {
        return debut;
    }

    public void setDebut(Date debut) {
        this.debut = debut;
    }

    public Date getFin() {
        return fin;
    }

    public void setFin(Date fin) {
        this.fin = fin;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Collection<Salle> getSalles() {
        return salles;
    }

    public void setSalles(Collection<Salle> salles) {
        this.salles = salles;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }
}
