package com.aau.moodle20.repository;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.ExerciseSheet;
import com.aau.moodle20.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseSheetRepository extends JpaRepository<ExerciseSheet,Long> {

    List<ExerciseSheet> findByCourse_Id(Long courseId);
}


