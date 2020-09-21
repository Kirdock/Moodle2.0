package com.aau.moodle20.entities;

import com.aau.moodle20.entity.Example;
import com.aau.moodle20.entity.ExerciseSheet;
import com.aau.moodle20.entity.FileType;
import com.aau.moodle20.entity.SupportFileType;
import com.aau.moodle20.payload.response.FileTypeResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(SpringRunner.class)
public class FileTypeEntityUnitTests {

    @Test
    public void test_FileType_createFileTypeResponseObject()  {
        FileType fileType = new FileType();
        fileType.setValue("test");
        fileType.setId(200L);
        fileType.setName("name");
        fileType.setAssignedExamples(new HashSet<>());

        FileTypeResponseObject testResponseObject = new FileTypeResponseObject();
        testResponseObject.setId(200L);
        testResponseObject.setName("name");
        testResponseObject.setValue("test");

        FileTypeResponseObject responseObject = fileType.createFileTypeResponseObject();

        assertEquals(testResponseObject.getId(),responseObject.getId());
        assertEquals(testResponseObject.getName(),responseObject.getName());
        assertEquals(testResponseObject.getValue(),responseObject.getValue());
    }
}
