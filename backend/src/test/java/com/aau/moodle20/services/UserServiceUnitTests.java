package com.aau.moodle20.services;

import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.ExampleRepository;
import com.aau.moodle20.repository.ExerciseSheetRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class UserServiceUnitTests {

    @InjectMocks
    private UserService userService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ExerciseSheetRepository exerciseSheetRepository;
    @Mock
    private ExampleRepository exampleRepository;


    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }
}
