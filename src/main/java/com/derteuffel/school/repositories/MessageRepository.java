package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by user on 01/04/2020.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {

    Collection<Message> findAllBySalle(String name);
    Collection<Message> findAllByCompte_Id(Long id);
    Collection<Message> findAllByVisibilite(String visibilite);
    Collection<Message> findAllByVisibiliteAndSalle(String visibilite, String name, Sort sort);
    Collection<Message> findAllByVisibilite(String visibilite, Sort sort);
    Collection<Message> findAllByVisibiliteAndSalle(String visibilite,String name, String ecole,Sort sort);
}
