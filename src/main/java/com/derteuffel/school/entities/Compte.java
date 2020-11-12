package com.derteuffel.school.entities;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by user on 22/03/2020.
 */
@Data
@Entity
@Table(name = "compte")
@OnDelete(action= OnDeleteAction.NO_ACTION)
public class Compte implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String password;
    private String email;
    private String avatar;
    private String resetToken;
    private String code;
    private String conferenceId;
    private String subscriberId;
    private Boolean status;
    private String bibliothequeCode;
    private String encode;
    private String type;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "comptes_roles",
            joinColumns = @JoinColumn(
                    name = "compte_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;


    @OneToOne(fetch = FetchType.LAZY)
    private Enseignant enseignant;

    @OneToOne(fetch = FetchType.LAZY)
    private Parent parent;

    @OneToMany(mappedBy = "compte")
    private Collection<Salaire> salaires;

   /* @OneToMany(mappedBy = "compte")
    private Collection<Cours> cours;*/

   @OneToOne(fetch = FetchType.LAZY)
   private Enfant enfant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getBibliothequeCode() {
        return bibliothequeCode;
    }

    public void setBibliothequeCode(String bibliothequeCode) {
        this.bibliothequeCode = bibliothequeCode;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public Collection<Salaire> getSalaires() {
        return salaires;
    }

    public void setSalaires(Collection<Salaire> salaires) {
        this.salaires = salaires;
    }
}
