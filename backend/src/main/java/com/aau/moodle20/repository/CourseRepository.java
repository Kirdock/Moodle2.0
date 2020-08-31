package com.aau.moodle20.repository;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Long> {

    List<Course> findCoursesBySemester_Id(Long  semesterId);

    List<Course> findCoursesBySemester_IdAndOwner_MatriculationNumber(Long semesterId, String matriculationNumber);

    Boolean existsByNumberAndSemester_Id(String number, Long semesterId);

    Boolean existsByNameAndNumber(String name, String number);

    Boolean existsByNumber(String number);

    Boolean existsByOwner_MatriculationNumber(String matriculationNumber);

    List<Course> findByOwner_MatriculationNumber(String matriculationNumber);

}


