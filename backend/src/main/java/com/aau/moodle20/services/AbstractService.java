package com.aau.moodle20.services;

import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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

    @Autowired
    ViolationHistoryRepository violationHistoryRepository;

    @Autowired
    ViolationRepository violationRepository;


    protected Semester readSemester(Long semesterId) {
        Optional<Semester> optionalSemester = semesterRepository.findById(semesterId);
        if (!optionalSemester.isPresent())
            throw new ServiceException("Error: Semester not found!",null, null, null, HttpStatus.NOT_FOUND);
        return optionalSemester.get();
    }

    protected Course readCourse(Long courseId) {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent())
            throw new ServiceException("Error: Course not found!", null,null, null, HttpStatus.NOT_FOUND);
        return optionalCourse.get();
    }

    protected User readUser(String matriculationNumber) {
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if (!optionalUser.isPresent())
            throw new ServiceException("Error: User not found!",null, null, null, HttpStatus.NOT_FOUND);
        return optionalUser.get();
    }

    protected ExerciseSheet readExerciseSheet(Long exerciseSheetId) {
        Optional<ExerciseSheet> optionalExerciseSheet = exerciseSheetRepository.findById(exerciseSheetId);
        if (!optionalExerciseSheet.isPresent())
            throw new ServiceException("Error: ExerciseSheet not found!",null, null, null, HttpStatus.NOT_FOUND);
        return optionalExerciseSheet.get();
    }

    protected Example readExample(Long exampleId) {
        Optional<Example> optionalExample = exampleRepository.findById(exampleId);
        if (!optionalExample.isPresent())
            throw new ServiceException("Error: Example not found!",null, null, null, HttpStatus.NOT_FOUND);
        return optionalExample.get();
    }

    protected UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected Boolean isAdmin() {
        return getUserDetails().getAdmin();
    }

    protected Boolean isOwner(Long courseId) {
        Course course = readCourse(courseId);
        return course.getOwner().getMatriculationNumber().equals(getUserDetails().getMatriculationNumber());
    }

    protected Boolean isOwner(Course course) {
        return course.getOwner().getMatriculationNumber().equals(getUserDetails().getMatriculationNumber());
    }

    protected User getCurrentUser() {
        return readUser(getUserDetails().getMatriculationNumber());
    }

    protected void saveFile(String filePath, MultipartFile file) throws IOException {
        File fileToBeSaved = new File(filePath);
        // if path does not exists create it
        if (!fileToBeSaved.exists()) {
            fileToBeSaved.mkdirs();
        }

        Path path = Paths.get(filePath , file.getOriginalFilename());
        Files.write(path, file.getBytes());
    }

    protected byte[] readFileFromDisk(String filePath, String fileName) throws IOException {
        Path path = Paths.get(filePath , fileName);
        return Files.readAllBytes(path);
    }

    protected boolean deleteFileFromDisk(String filePath, String fileName) throws IOException, ServiceException {
        Path path = Paths.get(filePath , fileName);
        return Files.deleteIfExists(path);
    }

    protected String createExampleAttachmentDir(Example example) {
        Semester semester = example.getExerciseSheet().getCourse().getSemester();
        Course course = example.getExerciseSheet().getCourse();
        ExerciseSheet exerciseSheet = example.getExerciseSheet();

        String semesterDir = "/" + semester.getType().name() + "_" + semester.getYear();
        String courseDir = "/" + course.getNumber();
        String exerciseSheetDir = "/" + exerciseSheet.getId();
        String exampleDir = "";
        if (example.getParentExample() != null)
            exampleDir = "/" + example.getParentExample().getId() + "/" + example.getId();
        else
            exampleDir = "/" + example.getId();

        return semesterDir + courseDir + exerciseSheetDir + exampleDir;
    }
}
