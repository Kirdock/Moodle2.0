package com.aau.moodle20.repository;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.ViolationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViolationHistoryRepository extends JpaRepository<ViolationHistory,Long> {



}


