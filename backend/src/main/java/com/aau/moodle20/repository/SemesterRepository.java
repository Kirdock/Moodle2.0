package com.aau.moodle20.repository;

import com.aau.moodle20.domain.ESemesterType;
import com.aau.moodle20.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester,Long> {

    Boolean existsByTypeAndYear(ESemesterType type, Integer year);
}

