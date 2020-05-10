package com.aau.moodle20.controller;

import com.aau.moodle20.domain.Course;
import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.security.services.SemesterService;
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

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(value = "/semester/{semesterId}/courses")
    public ResponseEntity<Course> getCoursesFromSemester(@PathVariable("semesterId") long semesterId)  {
        return ResponseEntity.ok(semesterService.getCoursesFromSemester(semesterId));
    }


    //Course Section
    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<?> createCourse(@Valid  @RequestBody CreateCourseRequest createCourseRequest)  throws SemesterException {

        semesterService.createCourse(createCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully created!"));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course/assign")
    public ResponseEntity<?> assignCourseToSemester(@Valid  @RequestBody AssignUserToCourseRequest assignUserToCourseRequest)  throws SemesterException {

        semesterService.assignCourse(assignUserToCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully assigned to user!"));
    }
}
