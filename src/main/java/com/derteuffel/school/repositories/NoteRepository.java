package com.derteuffel.school.repositories;

import com.derteuffel.school.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>{

    List<Note> findAllByEleve_Id(Long id);
    List<Note> findAllByMatiere_Id(Long id);
    List<Note> findAllByPeriodeAndMatiere_Id(String periode, Long id);
    List<Note> findAllByPeriodeAndEleve_Id(String periode, Long id);
}
