package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement,Long>{

    Paiement findByEleve_Id(Long id);
}
