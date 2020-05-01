package com.aau.moodle20.security.services;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemesterService {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    CourseRepository courseRepository;

    public void createSemester(CreateSemesterRequest createSemesterRequest) throws SemesterException
    {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear())) {
            throw new SemesterException("Error: Semester with this year and type already exists!" );
        }
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
    }

    public void createCourse(CreateCourseRequest createCourseRequest) throws SemesterException
    {
         Course course = new Course();
         course.setMinKreuzel(createCourseRequest.getMinKreuzel());
         course.setMinPoints(createCourseRequest.getMinPoints());
         course.setName(createCourseRequest.getName());
         course.setNumber(createCourseRequest.getNumber());
         course.setSemester(new Semester(createCourseRequest.getSemesterId()));

         courseRepository.save(course);
    }

    public List<Semester> getSemesters()
    {
        return semesterRepository.findAll();
    }
}
