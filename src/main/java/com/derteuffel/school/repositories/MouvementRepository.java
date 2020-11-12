package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Mouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MouvementRepository extends JpaRepository<Mouvement,Long>{

    Collection<Mouvement> findAllByCaisse_Id(Long id);
}
