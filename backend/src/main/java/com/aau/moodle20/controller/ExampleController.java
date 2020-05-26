package com.aau.moodle20.controller;

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
import com.aau.moodle20.services.ExampleService;
import com.aau.moodle20.services.ExerciseSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class ExampleController {

    @Autowired
    ExampleService exampleService;


    @GetMapping(value = "/fileTypes")
    public List<FileTypeResponseObject> getFileTypes()  {
        return exampleService.getFileTypes();
    }


    @PutMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> createExample(@Valid @RequestBody CreateExampleRequest createExampleRequest) throws ServiceValidationException {
        ExampleResponseObject exampleResponseObject = exampleService.createExample(createExampleRequest);
        return ResponseEntity.ok(exampleResponseObject);
    }

    @PostMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> updateExerciseSheet(@Valid @RequestBody UpdateExampleRequest updateExampleRequest) throws ServiceValidationException {
        ExampleResponseObject responseObject =  exampleService.updateExample(updateExampleRequest);
        return ResponseEntity.ok(responseObject);
    }

    // TODO refactor make own controller
    @DeleteMapping(value = "/example/{id}")
    public ResponseEntity<?> deleteExample(@PathVariable("id") long id) throws ServiceValidationException {
        exampleService.deleteExample(id);
        return ResponseEntity.ok(new MessageResponse("Example was successfully deleted!"));

    }

    @GetMapping(value = "/example/{id}")
    public ExampleResponseObject getExample(@PathVariable("id") long id) throws ServiceValidationException {
        return exampleService.getExample(id);
    }
}
