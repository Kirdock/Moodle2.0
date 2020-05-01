package com.aau.moodle20.security.services;

import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.exception.SemesterAlreadyCreatedException;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SemesterService {

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    CourseRepository courseRepository;

    public void createSemester(CreateSemesterRequest createSemesterRequest) throws SemesterAlreadyCreatedException
    {
        if (semesterRepository.existsByTypeAndYear(createSemesterRequest.getType(), createSemesterRequest.getYear())) {
            throw new SemesterAlreadyCreatedException("Error: Semester with this year and type already exists!" );
        }
        Semester semester = new Semester();
        semester.setType(createSemesterRequest.getType());
        semester.setYear(createSemesterRequest.getYear());

        semesterRepository.save(semester);
    }

    public List<Semester> getSemesters()
    {
        return semesterRepository.findAll();
    }
}
