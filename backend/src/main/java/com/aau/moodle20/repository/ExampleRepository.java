package com.aau.moodle20.repository;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleRepository extends JpaRepository<Example,Long> {

    List<Example> findByParentExample_id(Long parentExampleId);

    void deleteByIdIn(List<Long> ids);}


