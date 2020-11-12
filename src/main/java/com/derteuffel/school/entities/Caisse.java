package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "caisse")
public class Caisse implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    private String mois;
    private int annee;
    private Double soldeDebutmois;
    private Double soldeFinMois;
    private Double mouvementMensuel;
    private Boolean status;

    public Caisse() {
    }

    public Caisse(String mois, int annee, Double soldeDebutmois, Double soldeFinMois,
                  Double mouvementMensuel, Boolean status) {
        this.mois = mois;
        this.annee = annee;
        this.soldeDebutmois = soldeDebutmois;
        this.soldeFinMois = soldeFinMois;
        this.mouvementMensuel = mouvementMensuel;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public Double getSoldeDebutmois() {
        return soldeDebutmois;
    }

    public void setSoldeDebutmois(Double soldeDebutmois) {
        this.soldeDebutmois = soldeDebutmois;
    }

    public Double getSoldeFinMois() {
        return soldeFinMois;
    }

    public void setSoldeFinMois(Double soldeFinMois) {
        this.soldeFinMois = soldeFinMois;
    }

    public Double getMouvementMensuel() {
        return mouvementMensuel;
    }

    public void setMouvementMensuel(Double mouvementMensuel) {
        this.mouvementMensuel = mouvementMensuel;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
