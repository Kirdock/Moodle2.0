package com.aau.moodle20.repository;

import com.aau.moodle20.domain.UserCourseKey;
import com.aau.moodle20.domain.UserInCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInCourseRepository extends JpaRepository<UserInCourse, UserCourseKey> {

    List<UserInCourse> findByCourse_Id(Long courseId);

}


