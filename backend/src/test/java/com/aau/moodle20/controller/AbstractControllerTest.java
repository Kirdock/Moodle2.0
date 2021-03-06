package com.aau.moodle20.controller;

import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.Semester;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class AbstractControllerTest {

    @Value("${adminMatriculationNumber}")
    protected String adminMatriculationNumber;

    @Value("${jwtSecret}")
    protected String jwtSecret;

    @Value("${jwtExpirationMs}")
    protected int jwtExpirationMs;


    @MockBean
    protected UserRepository userRepository;

    @Autowired
    protected MockMvc mvc;
    protected Long courseId = 200L;


    protected ResultActions perform_Put(String uri, String jwtToken, String content) throws Exception {
        return this.mvc.perform(put(uri).header("Authorization",jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(content));
    }

    protected ResultActions perform_Post(String uri, String jwtToken, String content) throws Exception {
        return this.mvc.perform(post(uri).header("Authorization",jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8").content(content));
    }

    protected ResultActions perform_Get(String uri, String jwtToken) throws Exception {
        return this.mvc.perform(get(uri).header("Authorization",jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions perform_Delete(String uri, String jwtToken) throws Exception {
        return this.mvc.perform(delete(uri).header("Authorization",jwtToken)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    protected String  generateValidAdminJWToken()
    {
        return "Bearer "+ Jwts.builder()
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim(JwtUtils.CLAIM_IS_ADMIN,Boolean.TRUE)
                .claim(JwtUtils.CLAIM_MATRICULATION_NUMBER, adminMatriculationNumber)
                .claim(JwtUtils.CLAIM_FORENAME,"admin")
                .claim(JwtUtils.CLAIM_SURNAME,"admin")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


    protected String prepareAdminUser() {
        String jwtToken = generateValidAdminJWToken();
        User adminUser = getAdminUser();
        when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));

        return jwtToken;
    }

    protected User getAdminUser() {
        User user = new User();
        user.setForename("admin");
        user.setSurname("admin");
        user.setUsername("admin");
        user.setAdmin(Boolean.TRUE);
        user.setMatriculationNumber(adminMatriculationNumber);

        return user;
    }

    protected String  generateValidUserJWToken(User user)
    {
        return "Bearer "+ Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim(JwtUtils.CLAIM_IS_ADMIN,Boolean.FALSE)
                .claim(JwtUtils.CLAIM_MATRICULATION_NUMBER, user.getMatriculationNumber())
                .claim(JwtUtils.CLAIM_FORENAME,user.getForename())
                .claim(JwtUtils.CLAIM_SURNAME,user.getSurname())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }




    protected Course getTestCourse(User user) {
        Course course = new Course();
        course.setId(courseId);
        course.setDescription("dd");
        course.setDescriptionTemplate("dd");
        course.setId((long) 200);
        course.setMinKreuzel(20);
        course.setMinPoints(20);
        course.setName("dd");
        course.setNumber("123.456");
        course.setOwner(user);
        course.setSemester(new Semester(200L));
        course.setExerciseSheets(new HashSet<>());
        return course;
    }



    protected User getUser1() {
        User user = new User();
        user.setForename("user1_forename");
        user.setSurname("user1_surname");
        user.setUsername("user1");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("12345678");

        return user;
    }

    protected User getUser2() {
        User user = new User();
        user.setForename("user2_forename");
        user.setSurname("user2_surname");
        user.setUsername("user2");
        user.setAdmin(Boolean.FALSE);
        user.setMatriculationNumber("87654321");

        return user;
    }


    protected String  generateExpiredAdminJWToken()
    {
        LocalDate localDate = LocalDate.now().minusDays(2);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return "Bearer "+ Jwts.builder()
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(date)
                .claim(JwtUtils.CLAIM_IS_ADMIN,Boolean.TRUE)
                .claim(JwtUtils.CLAIM_MATRICULATION_NUMBER, adminMatriculationNumber)
                .claim(JwtUtils.CLAIM_FORENAME,"admin")
                .claim(JwtUtils.CLAIM_SURNAME,"admin")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
