package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SemesterService {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ExerciseSheetRepository exerciseSheetRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;

    @Autowired
    JwtUtils jwtUtils;


    public void createSemester(CreateSemesterRequest createSemesterRequest) throws ServiceValidationException {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear()))
            throw new ServiceValidationException("Error: Semester with this year and type already exists!", ApiErrorResponseCodes.SEMESTER_ALREADY_EXISTS);
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
    }

    public void createCourse(CreateCourseRequest createCourseRequest) throws SemesterException {

        checkIfSemesterExists(createCourseRequest.getSemesterId());
        if(courseRepository.existsByNameAndNumberAndSemester_Id(createCourseRequest.getName(),createCourseRequest.getNumber(),createCourseRequest.getSemesterId()))
            throw new ServiceValidationException("Course in Semester already exists",ApiErrorResponseCodes.COURSE_IN_SEMESTER_ALREADY_EXISTS);

        Course course = new Course();
        course.setMinKreuzel(createCourseRequest.getMinKreuzel());
        course.setMinPoints(createCourseRequest.getMinPoints());
        course.setName(createCourseRequest.getName());
        course.setNumber(createCourseRequest.getNumber());
        course.setSemester(new Semester(createCourseRequest.getSemesterId()));
        course.setOwner(new User(createCourseRequest.getOwner()));
        courseRepository.save(course);
    }

    public void updateCourse(UpdateCourseRequest updateCourseRequest) throws SemesterException {
        checkIfCourseExists(updateCourseRequest.getId());

        Course course = null;
        Optional<Course> optionalCourse = courseRepository.findById(updateCourseRequest.getId());
        if (optionalCourse.isPresent()) {
            course = optionalCourse.get();
            course.setMinKreuzel(updateCourseRequest.getMinKreuzel());
            course.setMinPoints(updateCourseRequest.getMinPoints());
            course.setName(updateCourseRequest.getName());
            course.setNumber(updateCourseRequest.getNumber());
        }
        courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) throws EntityNotFoundException {
        checkIfCourseExists(courseId);
        courseRepository.deleteById(courseId);
    }



    public void assignCourse(List<AssignUserToCourseRequest> assignUserToCourseRequests) throws SemesterException
    {
        //TODO add validation

        List<UserInCourse> userInCourses = new ArrayList<>();

        for(AssignUserToCourseRequest assignUserToCourseRequest: assignUserToCourseRequests) {

            UserCourseKey userCourseKey = new UserCourseKey();
            UserInCourse userInCourse = new UserInCourse();
            User user = new User();
            Course course = new Course();

            userCourseKey.setCourseId(assignUserToCourseRequest.getCourseId());
            userCourseKey.setMatriculationNumber(assignUserToCourseRequest.getMatriculationNumber());

            user.setMatriculationNumber(assignUserToCourseRequest.getMatriculationNumber());
            course.setId(assignUserToCourseRequest.getCourseId());
            userInCourse.setRole(assignUserToCourseRequest.getRole());
            userInCourse.setUser(user);
            userInCourse.setCourse(course);
            userInCourse.setId(userCourseKey);
            userInCourses.add(userInCourse);

        }
        userInCourseRepository.saveAll(userInCourses);
    }

    public List<Semester> getSemesters()
    {
        return semesterRepository.findAll();
    }

    public List<CourseResponseObject> getCoursesFromSemester(Long semesterId,String jwtToken) {
        checkIfSemesterExists(semesterId);
        Boolean isAdmin = jwtUtils.getAdminFromJwtToken(jwtToken.split(" ")[1].trim());
        String matriculationNumber = jwtUtils.getMatriculationNumberFromJwtToken(jwtToken.split(" ")[1].trim());
        List<CourseResponseObject> responseObjects = new ArrayList<>();
        List<Course> courses = null;

        if (isAdmin)
            courses = courseRepository.findCoursesBySemester_Id(semesterId);
        else
            courses = courseRepository.findCoursesBySemester_IdAndOwner_MatriculationNumber(semesterId, matriculationNumber);

        if (courses != null && !courses.isEmpty()) {
            for (Course course : courses) {
                CourseResponseObject responseObject = new CourseResponseObject();
                responseObject.setId(course.getId());
                responseObject.setName(course.getName());
                responseObject.setNumber(course.getNumber());
                responseObjects.add(responseObject);
            }
        }
        return responseObjects;
    }

    public CourseResponseObject getCourse (long courseId) throws SemesterException
    {
       checkIfCourseExists(courseId);
       Course course = courseRepository.findById(courseId).get();
       List<ExerciseSheet> exerciseSheets = exerciseSheetRepository.findByCourse_Id(courseId);

       CourseResponseObject responseObject = new CourseResponseObject();
       responseObject.setId(course.getId());
       responseObject.setName(course.getName());
       responseObject.setNumber(course.getNumber());
       responseObject.setMinKreuzel(course.getMinKreuzel());
       responseObject.setMinPoints(course.getMinPoints());
       responseObject.setExerciseSheets(exerciseSheets.stream().map(ExerciseSheet::getResponseObject).collect(Collectors.toList()));

       return responseObject;
    }

    protected void checkIfCourseExists(Long courseId) throws EntityNotFoundException {
        if (!courseRepository.existsById(courseId)) throw new EntityNotFoundException("Error: Course not found");
    }

    protected void checkIfSemesterExists(Long semesterId) throws EntityNotFoundException {
        if (!semesterRepository.existsById(semesterId)) throw new EntityNotFoundException("Error: Semester not found");
    }

    public void copyCourse(CopyCourseRequest copyCourseRequest) throws ServiceValidationException {
        Optional<Course> optionalCourse = courseRepository.findById(copyCourseRequest.getCourseId());
        if (!optionalCourse.isPresent())
            throw new ServiceValidationException("Error: Course not found", HttpStatus.NOT_FOUND);
        if (!semesterRepository.existsById(copyCourseRequest.getSemesterId()))
            throw new ServiceValidationException("Error: Semester not found", HttpStatus.NOT_FOUND);

        Course copiedCourse =optionalCourse.get().copyCourse();
        copiedCourse.setSemester(new Semester(copyCourseRequest.getSemesterId()));
        courseRepository.save(copiedCourse);
    }
}
