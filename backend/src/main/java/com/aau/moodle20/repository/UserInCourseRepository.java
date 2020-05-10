package com.aau.moodle20.repository;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.UserCourseKey;
import com.aau.moodle20.domain.UserInCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInCourseRepository extends JpaRepository<UserInCourse, UserCourseKey> {


}


