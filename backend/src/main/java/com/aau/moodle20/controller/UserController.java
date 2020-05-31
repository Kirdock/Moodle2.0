package com.aau.moodle20.controller;

import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.*;
import com.aau.moodle20.payload.response.*;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.services.FinishesExampleService;
import com.aau.moodle20.services.UserDetailsServiceImpl;
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

    @Autowired
    private FinishesExampleService finishesExampleService;

    // get api--------------------------------------------------------------------
    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(path = "/users")
    public List<UserResponseObject> getUsers() {
        return userDetailsService.getAllUses();
    }

    @GetMapping(path = "/users/course/{courseId}")
    public List<UserResponseObject> getUsersWithCourseRoles(@PathVariable("courseId") long courseId) {
        return userDetailsService.getUsersWithCourseRoles(courseId);
    }
    @GetMapping(path = "/user/isOwner")
    public Boolean isOwner() {
        return userDetailsService.isOwner();
    }

    @GetMapping(path = "/user/{matriculationNumber}")
    public UserResponseObject getUser(@PathVariable String matriculationNumber) {
        return userDetailsService.getUser(matriculationNumber);
    }

    // post api----------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping(path = "/user/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest, @RequestHeader("Authorization") String jwtToken) {
        userDetailsService.changePassword(changePasswordRequest, jwtToken);
        return ResponseEntity.ok(new MessageResponse("User password changed!"));
    }

    @PostMapping(path = "/user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        userDetailsService.updateUser(updateUserRequest);
        return ResponseEntity.ok(new MessageResponse("User was successfully updated!"));
    }

    @PostMapping(path = "/user/examplePresented")
    public ResponseEntity<?> setUserExamplePresented(@Valid @RequestBody UserExamplePresentedRequest userExamplePresentedRequest) {
        finishesExampleService.setUserExamplePresented(userExamplePresentedRequest);
        return ResponseEntity.ok(new MessageResponse("Presented flag was successfully updated!"));
    }

    // put api---------------------------------------------------------------------

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        userDetailsService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/users")
    public ResponseEntity<?> registerUsers(@Valid  @RequestParam("file") MultipartFile file) throws UserException {

        userDetailsService.registerUsers(file);
        return ResponseEntity.ok(new MessageResponse("Users registered successfully!"));
    }

    @PutMapping(path = "/user/kreuzel")
    public ResponseEntity<?> setKreuzelUser(@Valid @RequestBody UserKreuzelRequest userKreuzelRequest) {
        finishesExampleService.setKreuzelUser(userKreuzelRequest);
        return ResponseEntity.ok(new MessageResponse("Kreuzel was successfully set!"));
    }

    // delete api -------------------------------------------------------------------------
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping(path = "/user/{matriculationNumber}")
    public ResponseEntity<?> deleteUser(@PathVariable String matriculationNumber) {
        userDetailsService.deleteUser(matriculationNumber);
        return ResponseEntity.ok(new MessageResponse("User was deleted!"));
    }


}
