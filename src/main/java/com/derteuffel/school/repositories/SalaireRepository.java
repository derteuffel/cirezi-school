package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Salaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SalaireRepository extends JpaRepository<Salaire,Long>{

    Collection<Salaire> findAllByEnseignant_Id(Long id);
    Collection<Salaire> findAllByCompte_Id(Long id);
}
