package com.aau.moodle20.repository;

import com.aau.moodle20.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
}
