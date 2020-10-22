package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by user on 24/03/2020.
 */
@Repository
public interface SalleRepository extends JpaRepository<Salle,Long> {
Salle findByPrincipal(String name);

Collection<Salle> findAllByEnseignants_Id(Long id);
}
