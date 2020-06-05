package com.aau.moodle20.services;

import com.aau.moodle20.component.PdfHelper;
import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.entity.embeddable.SupportFileTypeKey;
import com.aau.moodle20.entity.embeddable.UserCourseKey;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
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
    UserRepository userRepository;

    @Autowired
    FileTypeRepository fileTypeRepository;
    @Autowired
    SupportFileTypeRepository supportFileTypeRepository;

    @Autowired
    ExampleRepository exampleRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PdfHelper pdfHelper;

    private static final Logger logger = LoggerFactory.getLogger(SemesterService.class);




    public void createSemester(CreateSemesterRequest createSemesterRequest) throws ServiceValidationException {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear()))
            throw new ServiceValidationException("Error: Semester with this year and type already exists!", ApiErrorResponseCodes.SEMESTER_ALREADY_EXISTS);
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
    }

    public CourseResponseObject createCourse(CreateCourseRequest createCourseRequest) throws ServiceValidationException {

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
        course.setIncludeThird(createCourseRequest.getIncludeThird()!=null?createCourseRequest.getIncludeThird():Boolean.FALSE);
        courseRepository.save(course);

        return new CourseResponseObject(course.getId());
    }

    public void updateCourse(UpdateCourseRequest updateCourseRequest) throws ServiceValidationException {

        UserDetailsImpl userDetails = getUserDetails();
        checkIfCourseExists(updateCourseRequest.getId());
        if (userDetails.getAdmin()&& updateCourseRequest.getOwner()!=null && !userRepository.existsByMatriculationNumber(updateCourseRequest.getOwner()))
            throw new ServiceValidationException("Error: Owner cannot be updated because the given matriculationNumber those not exists!", HttpStatus.NOT_FOUND);

        Course course = null;
        Optional<Course> optionalCourse = courseRepository.findById(updateCourseRequest.getId());
        if (!userDetails.getAdmin() &&  !userDetails.getMatriculationNumber().equals(optionalCourse.get().getOwner().getMatriculationNumber()))
            throw new ServiceValidationException("Error: User is not owner of this course and thus cannot update this course!", HttpStatus.UNAUTHORIZED);

        course = optionalCourse.get();
        course.setMinKreuzel(updateCourseRequest.getMinKreuzel());
        course.setMinPoints(updateCourseRequest.getMinPoints());
        course.setName(updateCourseRequest.getName());
        course.setNumber(updateCourseRequest.getNumber());
        course.setIncludeThird(updateCourseRequest.getIncludeThird());
        if (userDetails.getAdmin() && updateCourseRequest.getOwner()!=null)
            course.setOwner(new User(updateCourseRequest.getOwner()));

        courseRepository.save(course);
    }

    public void updateCourseDescriptionTemplate(UpdateCourseDescriptionTemplate updateCourseDescriptionTemplate) throws ServiceValidationException
    {
        if(!courseRepository.existsById(updateCourseDescriptionTemplate.getId()))
            throw new ServiceValidationException("Error: course not found",HttpStatus.NOT_FOUND);

        Course course = courseRepository.findById(updateCourseDescriptionTemplate.getId()).get();
        UserDetailsImpl userDetails = getUserDetails();
        if(!userDetails.getAdmin() && !userDetails.getMatriculationNumber().equals(course.getOwner().getMatriculationNumber()))
            throw new ServiceValidationException("Error: not authorized to update course",HttpStatus.UNAUTHORIZED);

        course.setDescriptionTemplate(updateCourseDescriptionTemplate.getDescription());
        courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) throws EntityNotFoundException {
        checkIfCourseExists(courseId);
        courseRepository.deleteById(courseId);
    }



    public void assignUsers(List<AssignUserToCourseRequest> assignUserToCourseRequests) throws SemesterException
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


    @Transactional
    public void assignFile(MultipartFile file, Long courseId) throws ServiceValidationException {
        if (file == null)
            throw new ServiceValidationException("Error: file is null");
        if (courseId == null)
            throw new ServiceValidationException("Error: file is null");
        checkIfCourseExists(courseId);
        List<User> allGivenUsers = userDetailsService.registerUsers(file);
        List<UserInCourse> userInCourses = new ArrayList<>();

        for (User user : allGivenUsers) {

            UserCourseKey userCourseKey = new UserCourseKey();
            UserInCourse userInCourse = new UserInCourse();
            Course course = new Course(courseId);

            userCourseKey.setCourseId(courseId);
            userCourseKey.setMatriculationNumber(user.getMatriculationNumber());

            userInCourse.setRole(ECourseRole.Student);
            userInCourse.setUser(user);
            userInCourse.setCourse(course);
            userInCourse.setId(userCourseKey);
            userInCourses.add(userInCourse);
        }
        userInCourseRepository.saveAll(userInCourses);
    }

    public List<Semester> getSemesters()
    {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();
        if(userDetails.getAdmin())
            semestersToBeReturned =  semesterRepository.findAll();
        else if(courseRepository.existsByOwner_MatriculationNumber(userDetails.getMatriculationNumber()))
        {
            List<Course> courses = courseRepository.findByOwner_MatriculationNumber(userDetails.getMatriculationNumber());
            List<Semester> semesters = courses.stream().map(course -> course.getSemester()).collect(Collectors.toList());
            for(Semester semester: semesters)
            {
                if(!semestersToBeReturned.contains(semester))
                    semestersToBeReturned.add(semester);
            }
        }
        return semestersToBeReturned;
    }

    public List<Semester> getSemestersAssigned() {
        List<Semester> semestersToBeReturned = new ArrayList<>();
        UserDetailsImpl userDetails = getUserDetails();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUser_MatriculationNumber(userDetails.getMatriculationNumber());
        List<Semester> semesters = userInCourses.stream().map(userInCourse -> userInCourse.getCourse().getSemester()).collect(Collectors.toList());
        for (Semester semester : semesters) {
            if (!semestersToBeReturned.contains(semester))
                semestersToBeReturned.add(semester);
        }

        return semestersToBeReturned;
    }

    public List<CourseResponseObject> getCoursesFromSemester(Long semesterId) {
        checkIfSemesterExists(semesterId);

        UserDetailsImpl userDetails = getUserDetails();

        List<CourseResponseObject> responseObjects = new ArrayList<>();
        List<Course> courses = null;

        if (userDetails.getAdmin())
            courses = courseRepository.findCoursesBySemester_Id(semesterId);
        else
            courses = courseRepository.findCoursesBySemester_IdAndOwner_MatriculationNumber(semesterId, userDetails.getMatriculationNumber());

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
        checkIfSemesterExists(semesterId);
        UserDetailsImpl userDetails = getUserDetails();

        List<CourseResponseObject> responseObjects = new ArrayList<>();

        List<UserInCourse> userInCourses = userInCourseRepository.findByUser_MatriculationNumber(userDetails.getMatriculationNumber());

        if (userInCourses != null) {
            responseObjects.addAll(userInCourses.stream()
                    .filter(userInCourse -> userInCourse.getCourse().getSemester().getId().equals(semesterId))
                    .map(userInCourse -> userInCourse.getCourse().createCourseResponseObject())
                    .collect(Collectors.toList())
            );
        }
        return responseObjects;
    }

    public CourseResponseObject getCourseAssigned(Long courseId) throws ServiceValidationException {
        checkIfCourseExists(courseId);
        UserDetailsImpl userDetails = getUserDetails();
        Optional<User> user = userRepository.findByMatriculationNumber(userDetails.getMatriculationNumber());
        if (user.isPresent()) {
            Boolean isCourseAssigned = user.get().getCourses().stream().anyMatch(userInCourse -> userInCourse.getCourse().getId().equals(courseId));
            if(!isCourseAssigned)
                throw new ServiceValidationException("Error: User is not assigned to this course!",HttpStatus.UNAUTHORIZED);
        }
        Course course = courseRepository.findById(courseId).get();

        return course.createCourseResponseObject_GetAssignedCourse(userDetails.getMatriculationNumber());
    }

    public CourseResponseObject getCourse(long courseId) throws ServiceValidationException {
        UserDetailsImpl userDetails = getUserDetails();
        List<FinishesExampleResponse> finishesExampleResponses = new ArrayList<>();
        List<FinishesExample> finishesExamples = new ArrayList<>();

        Course course = checkIfCourseExists(courseId);
        if (!userDetails.getAdmin() && !course.getOwner().getMatriculationNumber().equals(userDetails.getMatriculationNumber()))
            throw new ServiceValidationException("Error: neither admin or owner", HttpStatus.UNAUTHORIZED);

        CourseResponseObject responseObject = course.createCourseResponseObject_GetCourse();
        for (ExerciseSheet exerciseSheet : course.getExerciseSheets()) {
            for (Example example : exerciseSheet.getExamples()) {
                finishesExamples.addAll(new ArrayList<>(example.getExamplesFinishedByUser()));
            }
        }

        for (FinishesExample finishesExample : finishesExamples) {
            FinishesExampleResponse finishesExampleResponse = new FinishesExampleResponse();
            finishesExampleResponse.setMatriculationNumber(finishesExample.getUser().getMatriculationNumber());
            finishesExampleResponse.setSurname(finishesExample.getUser().getSurname());
            finishesExampleResponse.setForname(finishesExample.getUser().getForename());
            finishesExampleResponse.setExampleId(finishesExample.getExample().getId());
            finishesExampleResponse.setExampleName(finishesExample.getExample().getName());
            finishesExampleResponses.add(finishesExampleResponse);
        }

        responseObject.setPresented(finishesExampleResponses);

        if (userDetails.getAdmin())
            responseObject.setOwner(course.getOwner().getMatriculationNumber());

        return responseObject;
    }

    public ByteArrayInputStream generateCourseAttendanceList(Long courseId) throws ServiceValidationException {
        checkIfCourseExists(courseId);
        Course course = courseRepository.findById(courseId).get();
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfWriter.getInstance(document, out);
            document.open();

            pdfHelper.addAttendanceTitle(document);
            pdfHelper.addAttendanceTable(document, course);
            /*
            document.add(headerTable);
            document.add(table);*/

            document.close();

        } catch (DocumentException ex) {
            logger.error("Error occurred: {0}", ex);
            throw new ServiceValidationException(ex.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }





    protected Course checkIfCourseExists(Long courseId) throws ServiceValidationException {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) throw new ServiceValidationException("Error: Course not found",HttpStatus.NOT_FOUND);

        return optionalCourse.get();
    }

    protected void checkIfSemesterExists(Long semesterId) throws ServiceValidationException {
        if (!semesterRepository.existsById(semesterId)) throw new ServiceValidationException("Error: Semester not found",HttpStatus.NOT_FOUND);
    }

    @Transactional
    public CourseResponseObject copyCourse(CopyCourseRequest copyCourseRequest) throws ServiceValidationException {
        Optional<Course> optionalCourse = courseRepository.findById(copyCourseRequest.getCourseId());
        if (!optionalCourse.isPresent())
            throw new ServiceValidationException("Error: Course not found", HttpStatus.NOT_FOUND);
        if (!semesterRepository.existsById(copyCourseRequest.getSemesterId()))
            throw new ServiceValidationException("Error: Semester not found", HttpStatus.NOT_FOUND);

        Course originalCourse = optionalCourse.get();

        // first copy course
        Course copiedCourse = originalCourse.copy();
        copiedCourse.setSemester(new Semester(copyCourseRequest.getSemesterId()));
        courseRepository.save(copiedCourse);

        if (originalCourse.getExerciseSheets() == null || originalCourse.getExerciseSheets().isEmpty())
        {
            CourseResponseObject courseResponseObject = new CourseResponseObject();
            courseResponseObject.setId(copiedCourse.getId());
            return courseResponseObject;
        }

        for (ExerciseSheet exerciseSheet : originalCourse.getExerciseSheets()) {
            // second copy exercise sheet
            ExerciseSheet copiedExerciseSheet = exerciseSheet.copy();
            copiedExerciseSheet.setCourse(copiedCourse);
            exerciseSheetRepository.save(copiedExerciseSheet);

            if (exerciseSheet.getExamples() == null)
                continue;

            for (Example example : exerciseSheet.getExamples()) {

                // if true example is subExample and this is handled below
                if (example.getParentExample() != null)
                    continue;

                Example copiedExample = example.copy();
                copiedExample.setExerciseSheet(copiedExerciseSheet);
                exampleRepository.save(copiedExample);

                if (example.getSubExamples() != null) {
                    for (Example subExample : example.getSubExamples()) {
                        Example copiedSubExample = subExample.copy();
                        copiedSubExample.setExerciseSheet(copiedExerciseSheet);
                        copiedSubExample.setParentExample(copiedExample);
                        exampleRepository.save(copiedSubExample);
                        if (subExample.getSupportFileTypes() != null) {
                            copySupportFileType(subExample.getSupportFileTypes(), copiedSubExample);
                        }
                    }
                }

                if (example.getSupportFileTypes() == null)
                    continue;

                copySupportFileType(example.getSupportFileTypes(), copiedExample);
            }
        }
        CourseResponseObject responseObject = new CourseResponseObject();
        responseObject.setId(copiedCourse.getId());
        return responseObject;
    }

    protected  void copySupportFileType(Set<SupportFileType> supportFileTypes, Example copiedExample)
    {
        for (SupportFileType supportFileType : supportFileTypes) {

            SupportFileType copiedSupportFileType = new SupportFileType();

            SupportFileTypeKey supportFileTypeKey = new SupportFileTypeKey();
            supportFileTypeKey.setExampleId(copiedExample.getId());
            supportFileTypeKey.setFileTypeId(supportFileType.getFileType().getId());

            copiedSupportFileType.setExample(copiedExample);
            copiedSupportFileType.setId(supportFileTypeKey);
            copiedSupportFileType.setFileType(supportFileType.getFileType());
            supportFileTypeRepository.save(copiedSupportFileType);
        }
    }

    public UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
