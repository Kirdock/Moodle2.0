package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.payload.request.ExampleOrderRequest;
import com.aau.moodle20.payload.request.ExampleRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.ExampleService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public ExampleController(ExampleService exampleService)
    {
        this.exampleService = exampleService;
    }

    @GetMapping(value = "/fileTypes")
    public List<FileTypeResponseObject> getFileTypes()  {
        return exampleService.getFileTypes();
    }

    @GetMapping(value = "/example/{id}")
    public ExampleResponseObject getExample(@PathVariable("id") long id) throws ServiceValidationException {
        return exampleService.getExample(id);
    }

    @GetMapping(value = "/example/{id}/validator")
    public ResponseEntity<InputStreamResource> getExampleValidator(@PathVariable("id") long id) throws ServiceValidationException, IOException {
        Example example = exampleService.getExampleValidator(id);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(example.getValidatorContent());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+example.getValidator());
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @PutMapping(value = "/example")
    public ResponseEntity<ExampleResponseObject> createExample(@Valid @RequestBody ExampleRequest createExampleRequest) throws ServiceValidationException {
        ExampleResponseObject exampleResponseObject = exampleService.createExample(createExampleRequest);
        return ResponseEntity.ok(exampleResponseObject);
    }

    @PostMapping(value = "/example")
    public ResponseEntity<?> updateExample(@Valid @RequestBody ExampleRequest updateExampleRequest) throws ServiceValidationException {
        exampleService.updateExample(updateExampleRequest);
        return ResponseEntity.ok("Example successfully updated");
    }

    @PostMapping(value = "/examples/order")
    public ResponseEntity<?> updateExampleOrder(@Valid @RequestBody List<ExampleOrderRequest> exampleOrderRequests) throws ServiceValidationException {
        exampleService.updateExampleOrder(exampleOrderRequests);
        return ResponseEntity.ok("Example order updated successfully");
    }

    @PostMapping(value = "/example/validator")
    public ResponseEntity<?> setExampleValidator(@RequestParam(value = "file",required = true) MultipartFile validator, @Valid  @RequestParam(value = "id",required = true) Long exampleId) throws ServiceValidationException, IOException {
        exampleService.setExampleValidator(validator,exampleId);
        return ResponseEntity.ok("Validator was successfully set");
    }


    @DeleteMapping(value = "/example/{id}")
    public ResponseEntity<?> deleteExample(@PathVariable("id") long id) throws ServiceValidationException {
        exampleService.deleteExample(id);
        return ResponseEntity.ok(new MessageResponse("Example was successfully deleted!"));

    }
    @DeleteMapping(value = "/example/{id}/validator")
    public ResponseEntity<?> setExampleValidator(@PathVariable("id") long id) throws ServiceValidationException, IOException {
        exampleService.deleteExampleValidator(id);
        return ResponseEntity.ok("Validator was successfully deleted");
    }

}
