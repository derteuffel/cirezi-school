package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "note")
@Data
public class Note implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    private int maxPeriode;
    private int examen;
    @NotNull
    private Float premierePeriode = 0F;
    @NotNull
    private Float deuxiemePeriode = 0F;
    @NotNull
    private Float troisiemePeriode = 0F;
    @NotNull
    private Float quatriemePeriode = 0F;
    @NotNull
    private Float examePremier = 0F;
    @NotNull
    private Float exameSecond = 0F;
    @NotNull
    private Float totalSemestrePremier = 0F;
    @NotNull
    private Float totalSemestreSecond = 0F;
    @NotNull
    private Float totalGeneral = 0F;
    private Date dateAjout = new Date();

    @ManyToOne
    private Matiere matiere;

    @ManyToOne
    private Eleve eleve;

    public Note() {
    }

    public Note(int maxPeriode, int examen, Float premierePeriode, Float deuxiemePeriode,
                Float troisiemePeriode, Float quatriemePeriode, Float examePremier,
                Float exameSecond, Float totalSemestrePremier, Float totalSemestreSecond,
                Float totalGeneral, Date dateAjout) {
        this.maxPeriode = maxPeriode;
        this.examen = examen;
        this.premierePeriode = premierePeriode;
        this.deuxiemePeriode = deuxiemePeriode;
        this.troisiemePeriode = troisiemePeriode;
        this.quatriemePeriode = quatriemePeriode;
        this.examePremier = examePremier;
        this.exameSecond = exameSecond;
        this.totalSemestrePremier = totalSemestrePremier;
        this.totalSemestreSecond = totalSemestreSecond;
        this.totalGeneral = totalGeneral;
        this.dateAjout = dateAjout;
    }

    public int getMaxPeriode() {
        return maxPeriode;
    }

    public void setMaxPeriode(int maxPeriode) {
        this.maxPeriode = maxPeriode;
    }

    public int getExamen() {
        return examen;
    }

    public void setExamen(int examen) {
        this.examen = examen;
    }

    public Date getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Date dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getPremierePeriode() {
        return premierePeriode;
    }

    public void setPremierePeriode(Float premierePeriode) {
        this.premierePeriode = premierePeriode;
    }

    public Float getDeuxiemePeriode() {
        return deuxiemePeriode;
    }

    public void setDeuxiemePeriode(Float deuxiemePeriode) {
        this.deuxiemePeriode = deuxiemePeriode;
    }

    public Float getTroisiemePeriode() {
        return troisiemePeriode;
    }

    public void setTroisiemePeriode(Float troisiemePeriode) {
        this.troisiemePeriode = troisiemePeriode;
    }

    public Float getQuatriemePeriode() {
        return quatriemePeriode;
    }

    public void setQuatriemePeriode(Float quatriemePeriode) {
        this.quatriemePeriode = quatriemePeriode;
    }

    public Float getExamePremier() {
        return examePremier;
    }

    public void setExamePremier(Float examePremier) {
        this.examePremier = examePremier;
    }

    public Float getExameSecond() {
        return exameSecond;
    }

    public void setExameSecond(Float exameSecond) {
        this.exameSecond = exameSecond;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public Eleve getEleve() {
        return eleve;
    }

    public void setEleve(Eleve eleve) {
        this.eleve = eleve;
    }

    public Float getTotalSemestrePremier() {
        return totalSemestrePremier;
    }

    public void setTotalSemestrePremier(Float totalSemestrePremier) {
        this.totalSemestrePremier = totalSemestrePremier;
    }

    public Float getTotalSemestreSecond() {
        return totalSemestreSecond;
    }

    public void setTotalSemestreSecond(Float totalSemestreSecond) {
        this.totalSemestreSecond = totalSemestreSecond;
    }

    public Float getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(Float totalGeneral) {
        this.totalGeneral = totalGeneral;
    }
}
