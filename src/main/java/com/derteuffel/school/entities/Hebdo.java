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

    @ManyToMany(fetch = FetchType.LAZY)
    private Collection<Salle> salles;

    @ManyToOne(fetch = FetchType.LAZY)
    private Compte compte;



}
