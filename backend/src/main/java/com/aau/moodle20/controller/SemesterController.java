package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.services.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class SemesterController {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    SemesterService semesterService;

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/semester")
    public ResponseEntity<?> createSemester(@Valid  @RequestBody CreateSemesterRequest createSemesterRequest)  throws SemesterException {

        semesterService.createSemester(createSemesterRequest);
        return ResponseEntity.ok(new MessageResponse("Semester was sucessfully created!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(value = "/semesters")
    public List<Semester> getSemesters()  {
        return semesterService.getSemesters();
    }


    @GetMapping(value = "/semester/{semesterId}/courses")
    public ResponseEntity<List<CourseResponseObject>> getCoursesFromSemester(@PathVariable("semesterId") long semesterId, @RequestHeader("Authorization") String jwtToken)  {
        return ResponseEntity.ok(semesterService.getCoursesFromSemester(semesterId,jwtToken));
    }


    @GetMapping(value = "/course/{courseId}")
    public ResponseEntity<CourseResponseObject> getCourse(@PathVariable("courseId") long courseId,@RequestHeader("Authorization") String jwtToken)  {
        return ResponseEntity.ok(semesterService.getCourse(courseId,jwtToken));
    }


    //Course Section
    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<?> createCourse(@Valid  @RequestBody CreateCourseRequest createCourseRequest)  throws SemesterException {

        semesterService.createCourse(createCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully created!"));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping(value = "/course")
    public ResponseEntity<?> updateCourse(@Valid  @RequestBody UpdateCourseRequest updateCourseRequest)  throws SemesterException {

        semesterService.updateCourse(updateCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully updated!"));
    }

    @PostMapping(value = "/course/template")
    public ResponseEntity<?> updateCourseDescriptionTemplate(@Valid  @RequestBody UpdateCourseDescriptionTemplate updateCourseDescriptionTemplate)  throws SemesterException {

        semesterService.updateCourseDescriptionTemplate(updateCourseDescriptionTemplate);
        return ResponseEntity.ok(new MessageResponse("Course Description Template was sucessfully updated!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping(value = "/course/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable("courseId") long courseId)  throws SemesterException {

        semesterService.deleteCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully deleted!"));
    }


    @PostMapping(value = "/course/assign")
    public ResponseEntity<?> assignCourseToSemester(  @RequestBody List<AssignUserToCourseRequest> assignUserToCourseRequests)  throws SemesterException {

        semesterService.assignCourse(assignUserToCourseRequests);
        return ResponseEntity.ok(new MessageResponse("User were sucessfully assigned to courses!"));
    }

    @PostMapping(value = "/course/copy")
    public ResponseEntity<?> assignCourseToSemester(  @RequestBody CopyCourseRequest copyCourseRequest)  throws ServiceValidationException {

        semesterService.copyCourse(copyCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was successfully copied!"));
    }
}
