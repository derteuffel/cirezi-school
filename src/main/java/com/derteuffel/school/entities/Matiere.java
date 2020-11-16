package com.derteuffel.school.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "matiere")
@Data
public class Matiere implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int noteMax;
    private String categorie;
    @ManyToOne
    private Enseignant enseignant;
    @ManyToOne
    private Salle salle;

    @OneToMany(mappedBy = "matiere")
    private List<Note> notes;

    public Matiere() {
    }

    public Matiere(String name, int noteMax, String categorie) {
        this.name = name;
        this.noteMax = noteMax;
        this.categorie = categorie;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
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

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public int getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(int noteMax) {
        this.noteMax = noteMax;
    }

}
