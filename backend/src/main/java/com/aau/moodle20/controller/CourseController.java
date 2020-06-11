package com.aau.moodle20.controller;

import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCourseDescriptionTemplate;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.services.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController()
@RequestMapping("/api")
public class CourseController {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    SemesterService semesterService;

    @GetMapping(value = "/course/{courseId}")
    public ResponseEntity<CourseResponseObject> getCourse(@PathVariable("courseId") long courseId)  {
        return ResponseEntity.ok(semesterService.getCourse(courseId));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest)  throws SemesterException {
        return ResponseEntity.ok(semesterService.createCourse(createCourseRequest));
    }

    @PostMapping(value = "/course")
    public ResponseEntity<?> updateCourse(@Valid  @RequestBody UpdateCourseRequest updateCourseRequest)  throws SemesterException {

        semesterService.updateCourse(updateCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was successfully updated!"));
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

    @PostMapping(value = "/course/copy")
    public ResponseEntity<?> copyCourse(@Valid  @RequestBody CopyCourseRequest copyCourseRequest)  throws ServiceValidationException {

        CourseResponseObject responseObject = semesterService.copyCourse(copyCourseRequest);
        return ResponseEntity.ok(responseObject);
    }
}
