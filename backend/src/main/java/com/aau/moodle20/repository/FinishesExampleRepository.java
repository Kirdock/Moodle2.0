package com.aau.moodle20.repository;

import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinishesExampleRepository extends JpaRepository<FinishesExample, FinishesExampleKey> {

    Optional<FinishesExample> findByExample_IdAndUser_MatriculationNumber(Long exampleId, String matriculationNumber);
}
