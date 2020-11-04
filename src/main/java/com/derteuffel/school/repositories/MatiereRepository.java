package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long>{

    List<Matiere> findAllByEnseignant_Id(Long id);
    List<Matiere> findAllBySalle_Id(Long id);
    Matiere findByNameAndSalle_Id(String name, Long id);
}
