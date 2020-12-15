package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "salaire")
public class Salaire implements Serializable{


    @Id
    @GeneratedValue
    private Long id;

    private int numBuletin;
    private String mois;
    private String datePaiement;
    private Double salaireBrut;
    private Double housing;
    private Double allocationFamilliale;
    private Double allocationTransport;
    private Double salaireBase;
    private Double taxe;
    private Double mutuelleSante;
    private Double cnss;
    private Double cafeteriat;
    private Double remboursementMensuelle;
    private Double netPaie;
    private Double avanceSalaire;
    private Double salaireNet;
    private String filePath;

    @ManyToOne
    private Enseignant enseignant;

    @ManyToOne
    private Compte compte;

    public Salaire() {
    }

    public Salaire(int numBuletin, String mois, String datePaiement, Double salaireBrut, Double housing,
                   Double allocationFamilliale, Double allocationTransport, Double salaireBase, Double taxe,
                   Double mutuelleSante, Double cnss, Double cafeteriat, Double remboursementMensuelle,
                   Double netPaie, Double avanceSalaire, Double salaireNet, String filePath) {
        this.numBuletin = numBuletin;
        this.mois = mois;
        this.datePaiement = datePaiement;
        this.salaireBrut = salaireBrut;
        this.housing = housing;
        this.allocationFamilliale = allocationFamilliale;
        this.allocationTransport = allocationTransport;
        this.salaireBase = salaireBase;
        this.taxe = taxe;
        this.mutuelleSante = mutuelleSante;
        this.cnss = cnss;
        this.cafeteriat = cafeteriat;
        this.remboursementMensuelle = remboursementMensuelle;
        this.netPaie = netPaie;
        this.avanceSalaire = avanceSalaire;
        this.salaireNet = salaireNet;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumBuletin() {
        return numBuletin;
    }

    public void setNumBuletin(int numBuletin) {
        this.numBuletin = numBuletin;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(String datePaiement) {
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

    public Double getTaxe() {
        return taxe;
    }

    public void setTaxe(Double taxe) {
        this.taxe = taxe;
    }

    public Double getMutuelleSante() {
        return mutuelleSante;
    }

    public void setMutuelleSante(Double mutuelleSante) {
        this.mutuelleSante = mutuelleSante;
    }

    public Double getCnss() {
        return cnss;
    }

    public void setCnss(Double cnss) {
        this.cnss = cnss;
    }

    public Double getCafeteriat() {
        return cafeteriat;
    }

    public void setCafeteriat(Double cafeteriat) {
        this.cafeteriat = cafeteriat;
    }

    public Double getRemboursementMensuelle() {
        return remboursementMensuelle;
    }

    public void setRemboursementMensuelle(Double remboursementMensuelle) {
        this.remboursementMensuelle = remboursementMensuelle;
    }

    public Double getNetPaie() {
        return netPaie;
    }

    public void setNetPaie(Double netPaie) {
        this.netPaie = netPaie;
    }

    public Double getAvanceSalaire() {
        return avanceSalaire;
    }

    public void setAvanceSalaire(Double avanceSalaire) {
        this.avanceSalaire = avanceSalaire;
    }

    public Double getSalaireNet() {
        return salaireNet;
    }

    public void setSalaireNet(Double salaireNet) {
        this.salaireNet = salaireNet;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
