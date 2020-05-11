package com.aau.moodle20.security.services;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.domain.UserCourseKey;
import com.aau.moodle20.domain.UserInCourse;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SemesterService {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;


    public void createSemester(CreateSemesterRequest createSemesterRequest) throws SemesterException
    {
        //TODO add more validation

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
        //TODO add validation

        Course course = new Course();
         course.setMinKreuzel(createCourseRequest.getMinKreuzel());
         course.setMinPoints(createCourseRequest.getMinPoints());
         course.setName(createCourseRequest.getName());
         course.setNumber(createCourseRequest.getNumber());
         course.setSemester(new Semester(createCourseRequest.getSemesterId()));

         courseRepository.save(course);
    }

    public void assignCourse(AssignUserToCourseRequest assignUserToCourseRequest) throws SemesterException
    {
        //TODO add validation

        UserCourseKey userCourseKey = new UserCourseKey();
        UserInCourse userInCourse = new UserInCourse();

        userCourseKey.setCourseId(assignUserToCourseRequest.getCourseId());
        userCourseKey.setMatrikelNummer(assignUserToCourseRequest.getMatrikelNummer());
        userInCourse.setId(userCourseKey);
        userInCourse.setRole(assignUserToCourseRequest.getCourseRole());

        userInCourseRepository.save(userInCourse);
    }

    public List<Semester> getSemesters()
    {
        return semesterRepository.findAll();
    }

    public List<Course> getCoursesFromSemester(Long semesterId)
    {
        //TODO add validation

        List<Course> courses =  courseRepository.findCoursesBySemester(new Semester(semesterId));

        return courses;
    }
}
