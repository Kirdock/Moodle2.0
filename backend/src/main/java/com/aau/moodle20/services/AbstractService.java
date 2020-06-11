package com.aau.moodle20.services;

import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.OptionalInt;

public class AbstractService {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ExerciseSheetRepository exerciseSheetRepository;

    @Autowired
    ExampleRepository exampleRepository;

    @Autowired
    SupportFileTypeRepository supportFileTypeRepository;

    @Autowired
    FileTypeRepository fileTypeRepository;

    @Autowired
    FinishesExampleRepository finishesExampleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;


    protected Semester readSemester(Long semesterId) throws ServiceValidationException {
        Optional<Semester> optionalSemester = semesterRepository.findById(semesterId);
        if (!optionalSemester.isPresent())
            throw new ServiceValidationException("Error: Semester not found!", HttpStatus.NOT_FOUND);
        return optionalSemester.get();
    }

    protected Course readCourse(Long courseId) throws ServiceValidationException {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent())
            throw new ServiceValidationException("Error: Course not found!", HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }

    protected User readUser(String matriculationNumber) throws ServiceValidationException
    {
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if(!optionalUser.isPresent())
            throw new ServiceValidationException("Error: User not found!",HttpStatus.NOT_FOUND);
        return optionalUser.get();
    }

    protected Example readExample(Long exampleId) throws ServiceValidationException {
        Optional<Example> optionalExample = exampleRepository.findById(exampleId);
        if (!optionalExample.isPresent())
            throw new ServiceValidationException("Error: Example not found!", HttpStatus.NOT_FOUND);
        return optionalExample.get();
    }
}
