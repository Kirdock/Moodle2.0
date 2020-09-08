package com.aau.moodle20;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Example;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.CourseResponseObject;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.services.CourseService;
import com.aau.moodle20.services.ExampleService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExampleControllerUnitTests  extends AbstractControllerTest {

    @MockBean
    private ExampleService exampleService;

    @MockBean
    private ExampleRepository exampleRepository;

    @MockBean
    private UserRepository userRepository;

    @Before
    public void mockExampleService_Methods() throws IOException, ClassNotFoundException {
        when(exampleService.getExample(anyLong())).thenReturn(new ExampleResponseObject());
        when(exampleService.getFileTypes()).thenReturn(new ArrayList<>());
        when(exampleService.getExampleValidator(anyLong())).thenReturn(new Example());
        when(exampleService.createExample(any(CreateExampleRequest.class))).thenReturn(new ExampleResponseObject());
        doNothing().when(exampleService).updateExample(any(UpdateExampleRequest.class));
        doNothing().when(exampleService).updateExampleOrder(new ArrayList<>());
        doNothing().when(exampleService).setExampleValidator(any(MultipartFile.class),anyLong());
        doNothing().when(exampleService).deleteExample(anyLong());
        doNothing().when(exampleService).deleteExampleValidator(anyLong());
    }
}
