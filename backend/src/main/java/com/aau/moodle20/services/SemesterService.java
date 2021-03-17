package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemesterService extends AbstractService {


    public void createSemester(CreateSemesterRequest createSemesterRequest) {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear()))
            throw new ServiceException("Error: Semester with this year and type already exists!", null, ApiErrorResponseCodes.SEMESTER_ALREADY_EXISTS, null, null);
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
    }

    public List<Semester> getSemesters() {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();
        if (userDetails.getAdmin())
            semestersToBeReturned = semesterRepository.findAll();
        else if (courseRepository.existsByOwnerMatriculationNumber(userDetails.getMatriculationNumber())) {
            List<Course> courses = courseRepository.findByOwnerMatriculationNumber(userDetails.getMatriculationNumber());
            List<Semester> semesters = courses.stream().map(Course::getSemester).collect(Collectors.toList());
            for (Semester semester : semesters) {
                if (!semestersToBeReturned.contains(semester))
                    semestersToBeReturned.add(semester);
            }
        }
        semestersToBeReturned.sort(Comparator.comparing(Semester::getYear).thenComparing(Semester::getType, Comparator.reverseOrder()));
        return semestersToBeReturned;
    }

    public List<Semester> getSemestersAssigned() {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber());
        List<Semester> semesters = userInCourses.stream().map(userInCourse -> userInCourse.getCourse().getSemester()).collect(Collectors.toList());
        for (Semester semester : semesters) {
            if (!semestersToBeReturned.contains(semester))
                semestersToBeReturned.add(semester);
        }
        semestersToBeReturned.sort(Comparator.comparing(Semester::getYear).thenComparing(Semester::getType, Comparator.reverseOrder()));

        return semestersToBeReturned;
    }

    public List<CourseResponseObject> getCoursesFromSemester(Long semesterId) {
        readSemester(semesterId);
        UserDetailsImpl userDetails = getUserDetails();
        List<CourseResponseObject> responseObjects = new ArrayList<>();
        List<Course> courses = null;

        if (userDetails.getAdmin())
            courses = courseRepository.findCoursesBySemesterId(semesterId);
        else
            courses = courseRepository.findCoursesBySemesterIdAndOwnerMatriculationNumber(semesterId, userDetails.getMatriculationNumber());

        if (courses != null && !courses.isEmpty()) {
            for (Course course : courses) {
                CourseResponseObject courseResponseObject = course.createCourseResponseObject();
                courseResponseObject.setOwner(null);
                responseObjects.add(courseResponseObject);
            }
        }
        return responseObjects;
    }

    public List<CourseResponseObject> getAssignedCoursesFromSemester(Long semesterId) {
        readSemester(semesterId);
        UserDetailsImpl userDetails = getUserDetails();

        List<CourseResponseObject> responseObjects = new ArrayList<>();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUserMatriculationNumber(userDetails.getMatriculationNumber());

        if (userInCourses != null) {
            responseObjects.addAll(userInCourses.stream()
                    .filter(userInCourse -> userInCourse.getCourse().getSemester().getId().equals(semesterId))
                    .filter(userInCourse -> !ECourseRole.NONE.equals(userInCourse.getRole()))
                    .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject())
                    .collect(Collectors.toList())
            );
        }
        return responseObjects;
    }


}
