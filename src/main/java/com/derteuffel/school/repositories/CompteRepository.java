package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by user on 22/03/2020.
 */
@Repository
public interface CompteRepository extends JpaRepository<Compte,Long> {

    Compte findByUsername(String username);
    Compte findByEmail(String email);
    Compte findByEnseignant_Id(Long id);
    Compte findByEnfant_Id(Long id);
    Compte findByParent_Id(Long id);
    Collection<Compte> findAllByEmail(String email);
    List<Compte> findAllByType(String type);
    Optional<Compte> findByResetToken(String resettoken);
}
