package com.aau.moodle20.entities;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.UpdateCoursePresets;
import com.aau.moodle20.payload.request.UpdateCourseRequest;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.UserDetailsImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class SupportFileTypeEntityUnitTests {



//    @Before
//    public void initMocks(){
//        MockitoAnnotations.initMocks(this);
//    }
    private final Long exampleId = 100L;
    private final Long exerciseSheetId = 120L;



    @Test
    public void test_supportFileType_copy()  {
        SupportFileType fileType = getTestSupportFileType();
        SupportFileType copyFileType = fileType.copy();
        assertEquals(fileType,copyFileType);
    }

    private SupportFileType getTestSupportFileType() {
        SupportFileType supportFileType = new SupportFileType();
        supportFileType.setExample(getTestExample());
        supportFileType.setFileType(getTestFileType());

        return supportFileType;
    }

    private Example getTestExample() {
        Example example = new Example();
        example.setId(exampleId);
        example.setExerciseSheet(new ExerciseSheet(exerciseSheetId));
        example.setUploadCount(10);
        example.setMandatory(Boolean.FALSE);
        example.setValidator("test.jar");
        example.setDescription("dd");
        example.setName("test");
        example.setOrder(2);
        example.setWeighting(20);
        example.setPoints(30);

        return example;
    }

    private FileType getTestFileType() {
        FileType fileType = new FileType();
        fileType.setName("word");
        fileType.setValue("*.docx");

        return fileType;
    }
}
