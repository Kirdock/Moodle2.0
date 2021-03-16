package com.aau.moodle20.repository;

import com.aau.moodle20.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findCoursesBySemesterId(Long semesterId);

    List<Course> findCoursesBySemesterIdAndOwnerMatriculationNumber(Long semesterId, String matriculationNumber);

    Boolean existsByNumberAndSemesterId(String number, Long semesterId);

    Boolean existsByOwnerMatriculationNumber(String matriculationNumber);

    List<Course> findByOwnerMatriculationNumber(String matriculationNumber);

}


