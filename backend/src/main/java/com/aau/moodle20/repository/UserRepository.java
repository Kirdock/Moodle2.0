package com.aau.moodle20.repository;

import com.aau.moodle20.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByMatriculationNumber(String matriculationNumber);

    Boolean existsByUsername(String username);

    Boolean existsByMatriculationNumber(String matriculationNumber);
}
