package com.aau.moodle20;

import com.aau.moodle20.entity.User;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AbstractControllerTest {

    @Value("${adminMatriculationNumber}")
    protected String adminMatriculationNumber;

    @Value("${jwtSecret}")
    protected String jwtSecret;

    @Value("${jwtExpirationMs}")
    protected int jwtExpirationMs;

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
