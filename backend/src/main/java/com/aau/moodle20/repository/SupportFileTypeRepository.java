package com.aau.moodle20.repository;

import com.aau.moodle20.entity.SupportFileType;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportFileTypeRepository extends JpaRepository<SupportFileType, SupportFileTypeKey> {


}


