package com.aau.moodle20.repository;

import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.embeddable.FinishesExampleKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinishesExampleRepository extends JpaRepository<FinishesExample, FinishesExampleKey> {

    Optional<FinishesExample> findByExampleIdAndUserMatriculationNumber(Long exampleId, String matriculationNumber);

    List<FinishesExample> findByExampleId(Long exampleId);

    Boolean existsByExampleIdAndUserMatriculationNumber(Long exampleId, String matriculationNumber);
}
