package com.derteuffel.school.helpers;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
public class SalaireHelper {
    private String mois;
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date datePaiement;
    private Double salaireBrut;
    private Double housing;
    private Double allocationFamilliale;
    private Double allocationTransport;
    private Double salaireBase;
    private Double avanceSalaire;


    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public Double getSalaireBrut() {
        return salaireBrut;
    }

    public void setSalaireBrut(Double salaireBrut) {
        this.salaireBrut = salaireBrut;
    }

    public Double getHousing() {
        return housing;
    }

    public void setHousing(Double housing) {
        this.housing = housing;
    }

    public Double getAllocationFamilliale() {
        return allocationFamilliale;
    }

    public void setAllocationFamilliale(Double allocationFamilliale) {
        this.allocationFamilliale = allocationFamilliale;
    }

    public Double getAllocationTransport() {
        return allocationTransport;
    }

    public void setAllocationTransport(Double allocationTransport) {
        this.allocationTransport = allocationTransport;
    }

    public Double getSalaireBase() {
        return salaireBase;
    }

    public void setSalaireBase(Double salaireBase) {
        this.salaireBase = salaireBase;
    }


    public Double getAvanceSalaire() {
        return avanceSalaire;
    }

    public void setAvanceSalaire(Double avanceSalaire) {
        this.avanceSalaire = avanceSalaire;
    }


}
