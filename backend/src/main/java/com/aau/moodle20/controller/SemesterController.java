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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;

@RestController()
@RequestMapping("/api")
public class SemesterController {

    @Autowired
    SemesterService semesterService;

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/semester")
    public ResponseEntity<?> createSemester(@Valid  @RequestBody CreateSemesterRequest createSemesterRequest)  throws SemesterException {
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
