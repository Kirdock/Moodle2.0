package com.aau.moodle20.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
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

    public ExerciseSheetController(ExerciseSheetService exerciseSheetService, PdfService pdfService) {
        this.exerciseSheetService = exerciseSheetService;
        this.pdfService = pdfService;
    }

    @PreAuthorize("hasPermission(#createExerciseSheetRequest.courseId, 'ExerciseSheet', 'create')")
    @PutMapping(value = "/exerciseSheet")
    public ResponseEntity<MessageResponse> createExerciseSheet(@Valid @RequestBody CreateExerciseSheetRequest createExerciseSheetRequest) {
        exerciseSheetService.createExerciseSheet(createExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully created!"));
    }

    @PreAuthorize("hasPermission(#id, 'ExerciseSheet', 'get')")
    @GetMapping(value = "/exerciseSheet/{id}")
    public ExerciseSheetResponseObject getExerciseSheets(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheet(id);
    }

    @PreAuthorize("hasPermission(#id, 'ExerciseSheet', 'get')")
    @GetMapping(value = "/exerciseSheet/{id}/kreuzel")
    public ExerciseSheetKreuzelResponse getExerciseSheetKreuzel(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheetKreuzel(id);
    }

    @GetMapping(value = "/exerciseSheetAssigned/{id}")
    public ExerciseSheetResponseObject getExerciseSheetAssigned(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheetAssigned(id);
    }

    @PreAuthorize("hasPermission(#updateExerciseSheetRequest.id, 'ExerciseSheet', 'update')")
    @PostMapping(value = "/exerciseSheet")
    public ResponseEntity<MessageResponse> updateExerciseSheet(@Valid @RequestBody UpdateExerciseSheetRequest updateExerciseSheetRequest) {
        exerciseSheetService.updateExerciseSheet(updateExerciseSheetRequest);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was sucessfully updated!"));
    }

    @PreAuthorize("hasPermission(#id, 'Course', 'get')")
    @GetMapping(value = "/course/{id}/exerciseSheets")
    public List<ExerciseSheetResponseObject> getExerciseSheetsFromCourse(@PathVariable("id") long id) {
        return exerciseSheetService.getExerciseSheetsFromCourse(id);
    }

    @PreAuthorize("hasPermission(#exerciseSheetId, 'ExerciseSheet', 'get')")
    @GetMapping(value = "/exerciseSheet/{id}/kreuzelList")
    public ResponseEntity<InputStreamResource> getKreuzelList(@PathVariable("id") long exerciseSheetId) throws IOException {

        ByteArrayInputStream bis = pdfService.generateKreuzelList(exerciseSheetId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + exerciseSheetId + "_kreuzelList.pdf");

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
        headers.add("Content-Disposition", "inline; filename=" + exerciseSheetId + "_document.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @PreAuthorize("hasPermission(#id, 'ExerciseSheet', 'delete')")
    @DeleteMapping(value = "/exerciseSheet/{id}")
    public ResponseEntity<MessageResponse> deleteExerciseSheet(@PathVariable("id") long id) throws IOException {
        exerciseSheetService.deleteExerciseSheet(id);
        return ResponseEntity.ok(new MessageResponse("ExerciseSheet was successfully deleted!"));
    }
}
