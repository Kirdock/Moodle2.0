package com.aau.moodle20.controller;

import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.ExerciseSheetService;
import com.aau.moodle20.services.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class ExerciseSheetController {

    @Autowired
    ExerciseSheetService exerciseSheetService;

    @PutMapping(value = "/exerciseSheet")
    public ResponseEntity<?> createExerciseSheet(@Valid  @RequestBody CreateExerciseSheetRequest createExerciseSheetRequest)  throws ServiceValidationException {

       exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully created!"));
    }


    @GetMapping(value = "/exerciseSheet/{id}")
    public ExerciseSheetResponseObject getSemesters(@PathVariable("id") long id)  {
        return exerciseSheetService.getExerciseSheet(id);
    }


    @PostMapping(value = "/exerciseSheet")
    public ResponseEntity<?> updateExerciseSheet(@Valid  @RequestBody UpdateExerciseSheetRequest updateExerciseSheetRequest)  throws ServiceValidationException, EntityNotFoundException {

        exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully updated!"));
    }


    @GetMapping(value = "/course/{id}/exerciseSheets")
    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(@PathVariable("id") long id) throws EntityNotFoundException
    {
        return exerciseSheetService.getExerciseSheetsFromCourse(id);
    }


}
