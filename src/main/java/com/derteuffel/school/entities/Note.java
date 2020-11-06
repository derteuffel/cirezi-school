package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "note")
@Data
public class Note implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    private String periode;
    private int note;
    private int noteMax;
    private Date dateAjout = new Date();

    @ManyToOne
    private Matiere matiere;

    @ManyToOne
    private Eleve eleve;

    public Note() {
    }

    public Note(String periode, int note, int noteMax, Date dateAjout) {
        this.periode = periode;
        this.note = note;
        this.noteMax = noteMax;
        this.dateAjout = dateAjout;
    }

    public int getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(int noteMax) {
        this.noteMax = noteMax;
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

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
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
}
