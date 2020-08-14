package com.aau.moodle20.controller;

import com.aau.moodle20.exception.EntityNotFoundException;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.CreateExerciseSheetRequest;
import com.aau.moodle20.payload.request.UpdateExerciseSheetRequest;
import com.aau.moodle20.payload.response.ExerciseSheetKreuzelResponse;
import com.aau.moodle20.payload.response.ExerciseSheetResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.ExerciseSheetService;
import com.aau.moodle20.services.PdfService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class ExerciseSheetController {

    ExerciseSheetService exerciseSheetService;
    PdfService pdfService;

    public ExerciseSheetController(ExerciseSheetService exerciseSheetService, PdfService pdfService)
    {
        this.exerciseSheetService= exerciseSheetService;
        this.pdfService = pdfService;
    }

    @PutMapping(value = "/exerciseSheet")
    public ResponseEntity<?> createExerciseSheet(@Valid @RequestBody CreateExerciseSheetRequest createExerciseSheetRequest) throws ServiceValidationException {
        exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully created!"));
    }

    @GetMapping(value = "/exerciseSheet/{id}")
    public ExerciseSheetResponseObject getExerciseSheets(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheet(id);
    }

    @GetMapping(value = "/exerciseSheet/{id}/kreuzel")
    public ExerciseSheetKreuzelResponse getExerciseSheetKreuzel(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheetKreuzel(id);
    }

    @GetMapping(value = "/exerciseSheetAssigned/{id}")
    public ExerciseSheetResponseObject getExerciseSheetAssigned(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheetAssigned(id);
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

    @GetMapping(value = "/exerciseSheet/{id}/kreuzelList")
    public ResponseEntity<InputStreamResource> getKreuzelList(@PathVariable("id") long exerciseSheetId) throws IOException {

        ByteArrayInputStream bis = pdfService.generateKreuzelList(exerciseSheetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+exerciseSheetId+"_kreuzelList.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping(value = "/exerciseSheetAssigned/{id}/pdf")
    public ResponseEntity<InputStreamResource> getExerciseSheetDocument(@PathVariable("id") long exerciseSheetId) throws IOException {
        ByteArrayInputStream bis = pdfService.generateExerciseSheetDocument(exerciseSheetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+exerciseSheetId+"_document.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @DeleteMapping(value = "/exerciseSheet/{id}")
    public ResponseEntity<?> deleteExerciseSheet(@PathVariable("id") long id) throws IOException {
        exerciseSheetService.deleteExerciseSheet(id);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was successfully deleted!"));
    }
}
