package com.aau.moodle20.repository;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Long> {

    List<Course> findCoursesBySemester_Id(Long  semesterId);

    List<Course> findCoursesBySemester_IdAndOwner_MatrikelNummer(Long semesterId, String matrikelNummer);


    Boolean existsByNameAndNumberAndSemester_Id(String name,String number, Long semesterId);
}


