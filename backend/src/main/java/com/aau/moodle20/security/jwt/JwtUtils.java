package com.aau.moodle20.security.jwt;

import com.aau.moodle20.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public static final String CLAIM_IS_ADMIN = "isAdmin";
    public static final String CLAIM_MATRICULATION_NUMBER = "matriculationNumber";
    public static final String CLAIM_FORENAME = "forename";
    public static final String CLAIM_SURNAME = "surname";


    @Value("${jwtSecret}")
    private String jwtSecret;

    @Value("${jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .claim(CLAIM_IS_ADMIN, userPrincipal.getAdmin())
                .claim(CLAIM_MATRICULATION_NUMBER, userPrincipal.getMatriculationNumber())
                .claim(CLAIM_FORENAME, userPrincipal.getForename())
                .claim(CLAIM_SURNAME, userPrincipal.getSurname())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public String getMatriculationNumberFromJwtToken(String token) {
        return (String) Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().get(CLAIM_MATRICULATION_NUMBER);
    }

    public Boolean getAdminFromJwtToken(String jwtToken) {
        return (Boolean) Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody().get(CLAIM_IS_ADMIN);
    }

    public boolean validateJwtToken(String authToken) throws Exception {
//        try {
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
        return true;
//        } catch (SignatureException e) {
//            logger.error("Invalid JWT signature: {}", e.getMessage());
//        } catch (MalformedJwtException e) {
//            logger.error("Invalid JWT token: {}", e.getMessage());
//        } catch (ExpiredJwtException e) {
//            logger.error("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            logger.error("JWT token is unsupported: {}", e.getMessage());
//        } catch (IllegalArgumentException e) {
//            logger.error("JWT claims string is empty: {}", e.getMessage());
//        }

//        return false;
    }
}
