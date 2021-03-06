package com.aau.moodle20.controller;

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

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(value = "/course/{courseId}")
    public ResponseEntity<CourseResponseObject> getCourse(@PathVariable("courseId") long courseId) {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(value = "/course/{courseId}/presented")
    public ResponseEntity<List<FinishesExampleResponse>> getCoursePresented(@PathVariable("courseId") long courseId) {
        return ResponseEntity.ok(courseService.getCoursePresented(courseId));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<CourseResponseObject> createCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest) {
        return ResponseEntity.ok(courseService.createCourse(createCourseRequest));
    }

    @PreAuthorize("hasPermission(#updateCourseRequest.id, 'Course', 'update')")
    @PostMapping(value = "/course")
    public ResponseEntity<MessageResponse> updateCourse(@Valid @RequestBody UpdateCourseRequest updateCourseRequest) {
        courseService.updateCourse(updateCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was successfully updated!"));
    }

    @PreAuthorize("hasPermission(#updateCoursePresets.id, 'Course', 'update')")
    @PostMapping(value = "/course/presets")
    public ResponseEntity<MessageResponse> updateCoursePresets(@Valid @RequestBody UpdateCoursePresets updateCoursePresets) {
        courseService.updateCoursePresets(updateCoursePresets);
        return ResponseEntity.ok(new MessageResponse("Course Presets were successfully updated!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping(value = "/course/{courseId}")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable("courseId") long courseId) throws IOException {

        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(new MessageResponse("Course was successfully deleted!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping(value = "/course/copy")
    public ResponseEntity<CourseResponseObject> copyCourse(@Valid @RequestBody CopyCourseRequest copyCourseRequest) throws IOException {

        CourseResponseObject responseObject = courseService.copyCourse(copyCourseRequest);
        return ResponseEntity.ok(responseObject);
    }
}
