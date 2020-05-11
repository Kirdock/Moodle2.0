package com.aau.moodle20.controller;

import com.aau.moodle20.domain.User;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.response.JwtResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.security.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;





    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        String password = "password";//TODO should not be hardcoded
        if(signUpRequest.getPassword()!=null && !signUpRequest.getPassword().isEmpty())
        {
            password = encoder.encode(signUpRequest.getPassword());
        }else
        {
            password = encoder.encode(password);
        }
        //username, matrikelNumber, forename, surename, password, isAdmin
        User user = new User(signUpRequest.getUsername(), signUpRequest.getMatrikelnummer(),signUpRequest.getForename(),signUpRequest.getSurname(),password,Boolean.FALSE);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/users")
    public ResponseEntity<?> registerUsers(@Valid  @RequestParam("file") MultipartFile file) throws UserException {

        userDetailsService.registerUsers(file);
        return ResponseEntity.ok(new MessageResponse("Users registered successfully!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(path = "/users")
    public List<User> getUsers() {

        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(User::getAdmin);

        return allUsers;
     }
}
