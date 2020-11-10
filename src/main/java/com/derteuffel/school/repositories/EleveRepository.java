package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by user on 27/03/2020.
 */
@Repository
public interface EleveRepository extends JpaRepository<Eleve, Long> {

    Collection<Eleve> findAllBySalle_Id(Long id);
    Collection<Eleve> findAllByParent_Id(Long id);
    Collection<Eleve> findAllByCategorieAndSalle_Id(String categorie, Long id);
    Collection<Eleve> findAllBySalle_IdAndParent_Id(Long id, Long number);
}
