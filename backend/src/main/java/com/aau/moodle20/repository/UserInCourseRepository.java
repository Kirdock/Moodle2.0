package com.aau.moodle20.repository;

import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInCourseRepository extends JpaRepository<UserInCourse, UserCourseKey> {

    List<UserInCourse> findByCourse_Id(Long courseId);

    List<UserInCourse> findByUser_MatriculationNumber(String matriculationNumber);

    Optional<UserInCourse> findByUser_MatriculationNumberAndCourse_Id(String matriculationNumber, Long courseId);

}


