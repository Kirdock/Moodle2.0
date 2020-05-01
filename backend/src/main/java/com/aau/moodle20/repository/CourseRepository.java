package com.aau.moodle20.repository;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.ESemesterType;
import com.aau.moodle20.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course,Long> {


}


