package com.aau.moodle20.controller;

import com.aau.moodle20.domain.Semester;
import com.aau.moodle20.domain.User;
import com.aau.moodle20.exception.SemesterException;
import com.aau.moodle20.payload.request.CreateCourseRequest;
import com.aau.moodle20.payload.request.CreateSemesterRequest;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.response.JwtResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.SemesterRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.security.services.SemesterService;
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
public class BackendController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    SemesterService semesterService;



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
    public ResponseEntity<?> registerUser(@Valid  @RequestBody SignUpRequest signUpRequest) {
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
    public ResponseEntity<?> registerUsers(@Valid  @RequestParam("file") MultipartFile file) {
        List<User> users = new ArrayList<>();

        BufferedReader br;
        List<String> result = new ArrayList<>();
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        result.remove(0); // remove first line

        String password = encoder.encode("password");//TODO should not be hardcoded

        for (int i=0; i<result.size(); i++)
        {
            String [] columns = result.get(i).split(";");
            User user = new User();
            user.setUsername(columns[0]);
            user.setMartikelNumber(columns[1]);
            user.setSurname(columns[2]);
            user.setForename(columns[3]);
            user.setAdmin(Boolean.FALSE);
            user.setPassword(password);
            users.add(user);
        }



        users.removeIf(user -> userRepository.findByUsername(user.getUsername()).isPresent());
        userRepository.saveAll(users);

        return ResponseEntity.ok(new MessageResponse("Users registered successfully!"));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/semester")
    public ResponseEntity<?> createSemester(@Valid  @RequestBody CreateSemesterRequest createSemesterRequest)  throws SemesterException {

        semesterService.createSemester(createSemesterRequest);
        return ResponseEntity.ok(new MessageResponse("Semester was sucessfully created!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/course")
    public ResponseEntity<?> createCourse(@Valid  @RequestBody CreateCourseRequest createCourseRequest)  throws SemesterException {

        semesterService.createCourse(createCourseRequest);
        return ResponseEntity.ok(new MessageResponse("Course was sucessfully created!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(value = "/semesters")
    public List<Semester> getSemesters()  {
        return semesterService.getSemesters();
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(path = "/users")
    public List<User> getUsers() {

        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(User::getAdmin);

        return allUsers;
     }

    // @RequestMapping(path="/secured", method = RequestMethod.GET)
    // public @ResponseBody String getSecured() {
    //     LOG.info("GET successfully called on /secured resource");
    //     return SECURED_TEXT;
    // }

    // // Forwards all routes to FrontEnd except: '/', '/index.html', '/api', '/api/**'
    // // Required because of 'mode: history' usage in frontend routing, see README for further details
    // @RequestMapping(value = "{_:^(?!index\\.html|api).*$}")
    // public String redirectApi() {
    //     LOG.info("URL entered directly into the Browser, so we need to redirect...");
    //     return "forward:/";
    // }

}
