package com.aau.moodle20.controller;

import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.AssignUserToCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.services.PdfService;
import com.aau.moodle20.services.UserCourseService;
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
public class UserCourseController {

    private UserCourseService userCourseService;
    private PdfService pdfService;

    public UserCourseController(UserCourseService userCourseService, PdfService pdfService)
    {
        this.userCourseService = userCourseService;
        this.pdfService = pdfService;
    }

    @GetMapping(value = "/courseAssigned/{courseId}")
    public ResponseEntity<CourseResponseObject> isCourseAssigned(@PathVariable("courseId") long courseId)  {
        return ResponseEntity.ok(userCourseService.getCourseAssigned(courseId));
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(value = "/course/{courseId}/attendanceList")
    public ResponseEntity<InputStreamResource> getAttendanceList(@PathVariable("courseId") long courseId) throws IOException {

        ByteArrayInputStream bis = pdfService.generateCourseAttendanceList(courseId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+courseId+"_attendanceList.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }


    @PostMapping(value = "/course/assign")
    public ResponseEntity<?> assignCourseToSemester(@Valid @RequestBody List<AssignUserToCourseRequest> assignUserToCourseRequests) throws ServiceException, IOException {

        userCourseService.assignUsers(assignUserToCourseRequests);
        return ResponseEntity.ok(new MessageResponse("Users were successfully assigned to courses!"));
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'update')")
    @PostMapping(value = "/course/assignFile")
    public ResponseEntity<RegisterMultipleUserResponse> assignFile(@Valid  @RequestParam(value = "file",required = true) MultipartFile file, @RequestParam(value = "id",required = true) Long courseId)  throws ServiceException {
        return ResponseEntity.ok(userCourseService.assignFile(file,courseId));
    }
}
