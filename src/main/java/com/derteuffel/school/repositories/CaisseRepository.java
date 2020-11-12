package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaisseRepository extends JpaRepository<Caisse,Long>{

    Caisse findByAnneeAndMois(int annee, String mois);
    Caisse findByStatus(Boolean status);
}
