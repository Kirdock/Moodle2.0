package com.aau.moodle20.controller;

import com.aau.moodle20.entity.FileType;
import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.ExerciseSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class ExerciseSheetController {

    @Autowired
    ExerciseSheetService exerciseSheetService;

    @PutMapping(value = "/exerciseSheet")
    public ResponseEntity<?> createExerciseSheet(@Valid @RequestBody CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully created!"));
    }

    @GetMapping(value = "/exerciseSheet/{id}")
    public ExerciseSheetResponseObject getExerciseSheets(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheet(id);
    }

    @PostMapping(value = "/exerciseSheet")
    public ResponseEntity<?> updateExerciseSheet(@Valid @RequestBody UpdateExerciseSheetRequest updateExerciseSheetRequest) throws ServiceValidationException, EntityNotFoundException {
        exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully updated!"));
    }

    @GetMapping(value = "/course/{id}/exerciseSheets")
    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(@PathVariable("id") long id) throws EntityNotFoundException {
        return exerciseSheetService.getExerciseSheetsFromCourse(id);
    }

    @GetMapping(value = "/fileTypes")
    public List<FileTypeResponseObject> getFileTypes()  {
        return exerciseSheetService.getFileTypes();
    }

    @DeleteMapping(value = "/exerciseSheet/{id}")
    public ResponseEntity<?> deleteExerciseSheet(@PathVariable("id") long id) {
        exerciseSheetService.deleteExerciseSheet(id);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was successfully deleted!"));

    }
    @PutMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> createExample(@Valid @RequestBody CreateExampleRequest createExampleRequest) throws ServiceValidationException {
        ExampleResponseObject exampleResponseObject = exerciseSheetService.createExample(createExampleRequest);
        return ResponseEntity.ok(exampleResponseObject);
    }

    @PostMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> updateExerciseSheet(@Valid @RequestBody UpdateExampleRequest updateExampleRequest) throws ServiceValidationException {
        ExampleResponseObject responseObject =  exerciseSheetService.updateExample(updateExampleRequest);
        return ResponseEntity.ok(responseObject);
    }


    @DeleteMapping(value = "/example/{id}")
    public ResponseEntity<?> deleteExample(@PathVariable("id") long id) throws ServiceValidationException {
        exerciseSheetService.deleteExample(id);
        return ResponseEntity.ok(new MessageResponse("Example was successfully deleted!"));

    }

    @GetMapping(value = "/example/{id}")
    public ExampleResponseObject getExample(@PathVariable("id") long id) throws ServiceValidationException {
        return exerciseSheetService.getExample(id);
    }
}
