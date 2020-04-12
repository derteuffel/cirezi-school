package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Hebdo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by user on 10/04/2020.
 */
@Repository
public interface HebdoRepository extends JpaRepository<Hebdo, Long> {


    Collection<Hebdo> findAllBySalle_Id(Long id,Sort sort);
    Collection<Hebdo> findAllByCompte_Id(Long id);
    Collection<Hebdo> findAllByCompte_IdAndSalle_Id(Long compteId, Long salleId, Sort sort);

}
