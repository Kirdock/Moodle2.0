package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.payload.request.CreateExampleRequest;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.UpdateExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.ExampleService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class ExampleController {

    ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping(value = "/fileTypes")
    public List<FileTypeResponseObject> getFileTypes() {
        return exampleService.getFileTypes();
    }

    @PreAuthorize("hasPermission(#id, 'Example', 'get')")
    @GetMapping(value = "/example/{id}")
    public ExampleResponseObject getExample(@PathVariable("id") long id) {
        return exampleService.getExample(id);
    }

    @PreAuthorize("hasPermission(#id, 'Example', 'get')")
    @GetMapping(value = "/example/{id}/validator")
    public ResponseEntity<InputStreamResource> getExampleValidator(@PathVariable("id") long id) throws IOException {
        Example example = exampleService.getExampleValidator(id);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(example.getValidatorContent());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + example.getValidator());
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @PreAuthorize("hasPermission(#createExampleRequest.exerciseSheetId, 'Example', 'create')")
    @PutMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> createExample(@Valid @RequestBody CreateExampleRequest createExampleRequest) throws IOException {
        ExampleResponseObject exampleResponseObject = exampleService.createExample(createExampleRequest);
        return ResponseEntity.ok(exampleResponseObject);
    }

    @PreAuthorize("hasPermission(#updateExampleRequest.id, 'Example', 'update')")
    @PostMapping(value = "/example")
    public ResponseEntity<MessageResponse> updateExample(@Valid @RequestBody UpdateExampleRequest updateExampleRequest) throws IOException {
        exampleService.updateExample(updateExampleRequest);
        return ResponseEntity.ok(new MessageResponse("Example successfully updated"));
    }

    @PreAuthorize("hasPermission(#exampleOrderRequests, 'Example', 'update')")
    @PostMapping(value = "/examples/order")
    public ResponseEntity<MessageResponse> updateExampleOrder(@Valid @RequestBody List<ExampleOrderRequest> exampleOrderRequests) {
        exampleService.updateExampleOrder(exampleOrderRequests);
        return ResponseEntity.ok(new MessageResponse("Example order updated successfully"));
    }

    @PreAuthorize("hasPermission(#exampleId, 'Example', 'update')")
    @PostMapping(value = "/example/validator")
    public ResponseEntity<MessageResponse> setExampleValidator(@RequestParam(value = "file") MultipartFile validator, @Valid @RequestParam(value = "id") Long exampleId) throws IOException {
        exampleService.setExampleValidator(validator, exampleId);
        return ResponseEntity.ok(new MessageResponse("Validator was successfully set"));
    }

    @PreAuthorize("hasPermission(#id, 'Example', 'delete')")
    @DeleteMapping(value = "/example/{id}")
    public ResponseEntity<MessageResponse> deleteExample(@PathVariable("id") long id) throws IOException {
        exampleService.deleteExample(id);
        return ResponseEntity.ok(new MessageResponse("Example was successfully deleted!"));

    }

    @PreAuthorize("hasPermission(#id, 'Example', 'delete')")
    @DeleteMapping(value = "/example/{id}/validator")
    public ResponseEntity<MessageResponse> deleteExampleValidator(@PathVariable("id") long id) throws IOException {
        exampleService.deleteExampleValidator(id);
        return ResponseEntity.ok(new MessageResponse("Validator was successfully deleted"));
    }

}
