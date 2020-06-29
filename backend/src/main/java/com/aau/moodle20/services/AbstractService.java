package com.aau.moodle20.services;

import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceValidationException;
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

    protected User readUser(String matriculationNumber) throws ServiceValidationException {
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if (!optionalUser.isPresent())
            throw new ServiceValidationException("Error: User not found!", HttpStatus.NOT_FOUND);
        return optionalUser.get();
    }

    protected ExerciseSheet readExerciseSheet(Long exerciseSheetId) throws ServiceValidationException {
        Optional<ExerciseSheet> optionalExerciseSheet = exerciseSheetRepository.findById(exerciseSheetId);
        if (!optionalExerciseSheet.isPresent())
            throw new ServiceValidationException("Error: ExerciseSheet not found!", HttpStatus.NOT_FOUND);
        return optionalExerciseSheet.get();
    }

    protected Example readExample(Long exampleId) throws ServiceValidationException {
        Optional<Example> optionalExample = exampleRepository.findById(exampleId);
        if (!optionalExample.isPresent())
            throw new ServiceValidationException("Error: Example not found!", HttpStatus.NOT_FOUND);
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

    protected void saveFile(String filePath, MultipartFile file) throws IOException
    {
        File fileToBeSaved= new File(filePath);
        // if path does not exists create it
        if(!fileToBeSaved.exists()) {
            fileToBeSaved.mkdirs();
        }

        Path path = Paths.get(filePath + "/"+file.getOriginalFilename());
        Files.write(path,file.getBytes());
    }

    protected byte[] readFileFromDisk(String filePath, String fileName) throws IOException {
        Path path = Paths.get(filePath+"/"+fileName);
        return Files.readAllBytes(path);
    }

    protected boolean deleteFileFromDisk(String filePath, String fileName) throws IOException,ServiceValidationException {
        Path path = Paths.get(filePath+"/"+fileName);
        return Files.deleteIfExists(path);
    }
    protected String createExampleAttachmentDir(Example example)
    {
        Semester semester = example.getExerciseSheet().getCourse().getSemester();
        Course course = example.getExerciseSheet().getCourse();
        ExerciseSheet exerciseSheet = example.getExerciseSheet();

        String semesterDir = "/"+semester.getType().name()+"_"+semester.getYear();
        String courseDir = "/" +course.getNumber();
        String exerciseSheetDir = "/" + exerciseSheet.getId();
        String exampleDir = "";
        if(example.getParentExample()!=null)
            exampleDir = "/" + example.getParentExample().getId() + "/" + example.getId();
        else
            exampleDir = "/" + example.getId();

        return semesterDir + courseDir + exerciseSheetDir + exampleDir;
    }
}
