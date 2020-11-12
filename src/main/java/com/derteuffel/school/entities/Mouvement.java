package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "mouvement")
public class Mouvement implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    private Date createdDate = new Date();
    private String libelle;
    private String type;
    private Double montant;
    private Double soldeFin;
    private String numMouvement;

    @ManyToOne
    private Caisse caisse;

    public Mouvement() {
    }

    public Mouvement(Date createdDate, String libelle, String type,
                     Double montant, Double soldeFin, String numMouvement) {
        this.createdDate = createdDate;
        this.libelle = libelle;
        this.type = type;
        this.montant = montant;
        this.soldeFin = soldeFin;
        this.numMouvement = numMouvement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Double getSoldeFin() {
        return soldeFin;
    }

    public void setSoldeFin(Double soldeFin) {
        this.soldeFin = soldeFin;
    }

    public Caisse getCaisse() {
        return caisse;
    }

    public void setCaisse(Caisse caisse) {
        this.caisse = caisse;
    }

    public String getNumMouvement() {
        return numMouvement;
    }

    public void setNumMouvement(String numMouvement) {
        this.numMouvement = numMouvement;
    }
}
