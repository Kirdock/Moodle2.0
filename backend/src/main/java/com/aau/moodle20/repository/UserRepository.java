package com.aau.moodle20.repository;

import com.aau.moodle20.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByMatrikelNummer(String matrikelNumber);

    Boolean existsByUsername(String username);

    Boolean existsByMatrikelNummer(String matrikelNummer);


    List<User> findByCourses_Course_Id(Long courseId);
}
