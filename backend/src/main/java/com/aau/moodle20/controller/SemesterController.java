package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class SemesterController {

    SemesterService semesterService;

    public SemesterController(SemesterService semesterService)
    {
        this.semesterService = semesterService;
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/semester")
    public ResponseEntity<?> createSemester(@Valid  @RequestBody CreateSemesterRequest createSemesterRequest)  throws ServiceException {
        semesterService.createSemester(createSemesterRequest);
        return ResponseEntity.ok(new MessageResponse("Semester was sucessfully created!"));
    }

    @GetMapping(value = "/semesters")
    public List<Semester> getSemesters()  {
        return semesterService.getSemesters();
    }

    @GetMapping(value = "/semesters/assigned")
    public List<Semester> getSemestersAssigned()  {
        return semesterService.getSemestersAssigned();
    }

    @GetMapping(value = "/semester/{semesterId}/courses")
    public ResponseEntity<List<CourseResponseObject>> getCoursesFromSemester(@PathVariable("semesterId") long semesterId)  {
        return ResponseEntity.ok(semesterService.getCoursesFromSemester(semesterId));
    }

    @GetMapping(value = "/semester/{semesterId}/coursesAssigned")
    public ResponseEntity<List<CourseResponseObject>> getAssignedCoursesFromSemester(@PathVariable("semesterId") long semesterId)  {
        return ResponseEntity.ok(semesterService.getAssignedCoursesFromSemester(semesterId));
    }
}
