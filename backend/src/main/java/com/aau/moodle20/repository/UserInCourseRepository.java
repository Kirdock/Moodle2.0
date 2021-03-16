package com.aau.moodle20.repository;

import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInCourseRepository extends JpaRepository<UserInCourse, UserCourseKey> {

    List<UserInCourse> findByCourseId(Long courseId);

    List<UserInCourse> findByUserMatriculationNumber(String matriculationNumber);

    Optional<UserInCourse> findByUserMatriculationNumberAndCourseId(String matriculationNumber, Long courseId);

}


