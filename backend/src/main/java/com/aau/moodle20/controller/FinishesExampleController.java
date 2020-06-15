package com.aau.moodle20.controller;

import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.payload.request.UserExamplePresentedRequest;
import com.aau.moodle20.payload.request.UserKreuzeMultilRequest;
import com.aau.moodle20.payload.request.UserKreuzelRequest;
import com.aau.moodle20.payload.response.KreuzelResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.services.FinishesExampleService;
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
public class FinishesExampleController {

    private FinishesExampleService finishesExampleService;

    public FinishesExampleController(FinishesExampleService finishesExampleService)
    {
        this.finishesExampleService = finishesExampleService;
    }

    @PostMapping(path = "/user/kreuzel")
    public ResponseEntity<?> setKreuzelUser(@Valid @RequestBody List<UserKreuzelRequest> userKreuzelRequests) {
        finishesExampleService.setKreuzelUser(userKreuzelRequests);
        return ResponseEntity.ok(new MessageResponse("Kreuzel were successfully set!"));
    }

    @PostMapping(path = "/user/kreuzelMulti")
    public ResponseEntity<?> setKreuzelUserMulti(@Valid @RequestBody List<UserKreuzeMultilRequest> userKreuzelRequests) {
        finishesExampleService.setKreuzelUserMulti(userKreuzelRequests);
        return ResponseEntity.ok(new MessageResponse("Kreuzel of users were successfully set!"));
    }

    @PostMapping(path = "/user/kreuzel/attachment")
    public ResponseEntity<?> setKreuzelUserAttachment(@Valid  @RequestParam(value = "file",required = true) MultipartFile file, @Valid  @RequestParam(value = "id",required = true) Long exampleId) throws IOException {
        finishesExampleService.setKreuzelUserAttachment(file,exampleId);
        return ResponseEntity.ok(new MessageResponse("Attachment for kreuzel was successfully set!"));
    }

    @GetMapping(path = "/user/kreuzel/attachment/{exampleId}")
    public ResponseEntity<InputStreamResource> getUserKreuzelAttachment(@PathVariable Long exampleId) {
        FinishesExample example = finishesExampleService.getKreuzelAttachment(exampleId);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(example.getAttachmentContent());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+example.getFileName());
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping(path = "/user/examplePresented")
    public ResponseEntity<?> setUserExamplePresented(@Valid @RequestBody UserExamplePresentedRequest userExamplePresentedRequest) {
        finishesExampleService.setUserExamplePresented(userExamplePresentedRequest);
        return ResponseEntity.ok(new MessageResponse("Presented flag was successfully updated!"));
    }

    @GetMapping(path = "/user/{matriculationNumber}/kreuzel/{courseId}")
    public List<KreuzelResponse> getKreuzelUserCourse(@PathVariable String matriculationNumber, @PathVariable Long courseId) {
        return finishesExampleService.getKreuzelUserCourse(matriculationNumber,courseId);
    }
}
