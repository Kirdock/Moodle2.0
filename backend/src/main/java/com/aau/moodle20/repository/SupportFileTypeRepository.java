package com.aau.moodle20.repository;

import com.aau.moodle20.entity.SupportFileType;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportFileTypeRepository extends JpaRepository<SupportFileType, SupportFileTypeKey> {


}


