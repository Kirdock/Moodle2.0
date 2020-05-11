package com.aau.moodle20.repository;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.ESemesterType;
import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Column;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course,Long> {

    List<Course> findCoursesBySemester(Semester semester);
}


