package com.aau.moodle20.repository;

import com.aau.moodle20.entity.FileType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileTypeRepository extends JpaRepository<FileType, Long> {

    Boolean existsByName(String name);
}


