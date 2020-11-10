package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;

@Data
@Entity
@Table(name = "paiement")
public class Paiement implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    private String category;
    private Double coutTotal = 0.0;
    private Double accountTrimestrePremier = 0.0;
    private Double accountTrimestreSecond = 0.0;
    private Double accountTrimestreTroisieme = 0.0;
    private Double accountBibliotheque = 0.0;
    private Double accountSport = 0.0;
    private Double totalPayer = 0.0;
    private Double solde = 0.0;

    @OneToOne(fetch = FetchType.LAZY)
    private Eleve eleve;

    public Paiement() {
    }

    public Paiement(String category, Double coutTotal, Double accountTrimestrePremier, Double accountTrimestreSecond, Double accountTrimestreTroisieme,
                    Double accountBibliotheque, Double accountSport, Double totalPayer, Double solde) {
        this.category = category;
        this.coutTotal = coutTotal;
        this.accountTrimestrePremier = accountTrimestrePremier;
        this.accountTrimestreSecond = accountTrimestreSecond;
        this.accountTrimestreTroisieme = accountTrimestreTroisieme;
        this.accountBibliotheque = accountBibliotheque;
        this.accountSport = accountSport;
        this.totalPayer = totalPayer;
        this.solde = solde;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getCoutTotal() {
        return coutTotal;
    }

    public void setCoutTotal(Double coutTotal) {
        this.coutTotal = coutTotal;
    }

    public Double getAccountTrimestrePremier() {
        return accountTrimestrePremier;
    }

    public void setAccountTrimestrePremier(Double accountTrimestrePremier) {
        this.accountTrimestrePremier = accountTrimestrePremier;
    }

    public Double getAccountTrimestreSecond() {
        return accountTrimestreSecond;
    }

    public void setAccountTrimestreSecond(Double accountTrimestreSecond) {
        this.accountTrimestreSecond = accountTrimestreSecond;
    }

    public Double getAccountTrimestreTroisieme() {
        return accountTrimestreTroisieme;
    }

    public void setAccountTrimestreTroisieme(Double accountTrimestreTroisieme) {
        this.accountTrimestreTroisieme = accountTrimestreTroisieme;
    }

    public Double getAccountBibliotheque() {
        return accountBibliotheque;
    }

    public void setAccountBibliotheque(Double accountBibliotheque) {
        this.accountBibliotheque = accountBibliotheque;
    }

    public Double getAccountSport() {
        return accountSport;
    }

    public void setAccountSport(Double accountSport) {
        this.accountSport = accountSport;
    }


    public Double getTotalPayer() {
        return totalPayer;
    }

    public void setTotalPayer(Double totalPayer) {
        this.totalPayer = totalPayer;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }

    public Double getSolde() {
        return solde;
    }



    public Eleve getEleve() {
        return eleve;
    }

    public void setEleve(Eleve eleve) {
        this.eleve = eleve;
    }
}
