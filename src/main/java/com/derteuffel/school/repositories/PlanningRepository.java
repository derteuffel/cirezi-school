package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by user on 10/04/2020.
 */
@Repository
public interface PlanningRepository extends JpaRepository<Planning,Long> {

    Collection<Planning> findAllByHebdo_Id(Long id);
    Planning findByCoursAndDate(String cours, String date);
}
