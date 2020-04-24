package com.aau.moodle20.repository;

import com.aau.moodle20.domain.ERole;
import com.aau.moodle20.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(ERole name);
}
