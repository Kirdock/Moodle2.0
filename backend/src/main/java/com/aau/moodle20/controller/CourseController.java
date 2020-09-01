package com.aau.moodle20.controller;

import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CopyCourseRequest;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class CourseController {

    CourseService courseService;

    public CourseController(CourseService courseService)
    {
        this.courseService = courseService;
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(value = "/course/{courseId}")
    public ResponseEntity<CourseResponseObject> getCourse(@PathVariable("courseId") long courseId)  {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(value = "/course/{courseId}/presented")
    public ResponseEntity<List<FinishesExampleResponse>> getCoursePresented(@PathVariable("courseId") long courseId)  {
        return ResponseEntity.ok(courseService.getCoursePresented(courseId));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest)  throws SemesterException {
        return ResponseEntity.ok(courseService.createCourse(createCourseRequest));
    }

    @PreAuthorize("hasPermission(#updateCourseRequest, 'Course', 'update')")
    @PostMapping(value = "/course")
    public ResponseEntity<?> updateCourse(@Valid  @RequestBody UpdateCourseRequest updateCourseRequest)  throws SemesterException {
        courseService.updateCourse(updateCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was successfully updated!"));
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'update')")
    @PostMapping(value = "/course/presets")
    public ResponseEntity<?> updateCoursePresets(@Valid  @RequestBody UpdateCoursePresets updateCoursePresets)  throws SemesterException {
        courseService.updateCoursePresets(updateCoursePresets);
        return ResponseEntity.ok(new MessageResponse("Course Presets were successfully updated!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping(value = "/course/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable("courseId") long courseId) throws IOException {

        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("Course was successfully deleted!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping(value = "/course/copy")
    public ResponseEntity<?> copyCourse(@Valid  @RequestBody CopyCourseRequest copyCourseRequest) throws ServiceException, IOException {

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        return ResponseEntity.ok(responseObject);
    }
}
