package com.aau.moodle20.repository;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(ECourseRole name);
}
