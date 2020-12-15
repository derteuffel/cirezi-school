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
    private String bulletinPath;

    @ManyToOne(fetch = FetchType.LAZY)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    private Parent parent;

    public Eleve() {
    }

    public Eleve(String name, String prenom, String postnom, String telephoneTuteur, String nomCompletTuteur,
                 String emailTuteur, String whatsappTuteur, int age, String localisation,
                 String autreInformation, String pays, String categorie, String bulletinPath) {
        this.name = name;
        this.prenom = prenom;
        this.postnom = postnom;
        this.telephoneTuteur = telephoneTuteur;
        this.nomCompletTuteur = nomCompletTuteur;
        this.emailTuteur = emailTuteur;
        this.whatsappTuteur = whatsappTuteur;
        this.age = age;
        this.localisation = localisation;
        this.autreInformation = autreInformation;
        this.pays = pays;
        this.categorie = categorie;
        this.bulletinPath = bulletinPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPostnom() {
        return postnom;
    }

    public void setPostnom(String postnom) {
        this.postnom = postnom;
    }

    public String getTelephoneTuteur() {
        return telephoneTuteur;
    }

    public void setTelephoneTuteur(String telephoneTuteur) {
        this.telephoneTuteur = telephoneTuteur;
    }

    public String getNomCompletTuteur() {
        return nomCompletTuteur;
    }

    public void setNomCompletTuteur(String nomCompletTuteur) {
        this.nomCompletTuteur = nomCompletTuteur;
    }

    public String getEmailTuteur() {
        return emailTuteur;
    }

    public void setEmailTuteur(String emailTuteur) {
        this.emailTuteur = emailTuteur;
    }

    public String getWhatsappTuteur() {
        return whatsappTuteur;
    }

    public void setWhatsappTuteur(String whatsappTuteur) {
        this.whatsappTuteur = whatsappTuteur;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getAutreInformation() {
        return autreInformation;
    }

    public void setAutreInformation(String autreInformation) {
        this.autreInformation = autreInformation;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getBulletinPath() {
        return bulletinPath;
    }

    public void setBulletinPath(String bulletinPath) {
        this.bulletinPath = bulletinPath;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
