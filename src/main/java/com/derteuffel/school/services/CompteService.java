package com.derteuffel.school.services;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.helpers.EncadrementRegistrationDto;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by user on 22/03/2020.
 */
public interface CompteService extends UserDetailsService{

    Compte findByUsername(String username);
    Compte findByEmail(String email);
    Compte save(String email, String password, String username, String s);
    Compte saveRoot(CompteRegistrationDto compteRegistrationDto, String s);
    Compte saveEnseignant(CompteRegistrationDto compteRegistrationDto, String s, Enseignant enseignant);
    Compte saveParent(CompteRegistrationDto compteRegistrationDto, String s, Parent parent);
    Compte saveEnfant(EncadrementRegistrationDto encadrementRegistrationDto, String s, Enfant enfant);
    Compte saveEncadreur(EncadrementRegistrationDto encadrementRegistrationDto, String s, Encadreur encadreur);
}
