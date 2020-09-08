package com.aau.moodle20.controller;

import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.JwtResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class UserController {
    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;
    private UserService userService;

    public UserController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService)
    {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    // get api--------------------------------------------------------------------
    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping(path = "/users")
    public List<UserResponseObject> getUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasPermission(#courseId, 'Course', 'get')")
    @GetMapping(path = "/users/course/{courseId}")
    public List<UserResponseObject> getUsersWithCourseRoles(@PathVariable("courseId") long courseId) {
        return userService.getUsersWithCourseRoles(courseId);
    }
    @GetMapping(path = "/user/isOwner")
    public Boolean isOwner() {
        return userService.isOwner();
    }

    @PreAuthorize("hasPermission(#matriculationNumber, 'User', 'get')")
    @GetMapping(path = "/user/{matriculationNumber}")
    public UserResponseObject getUser(@PathVariable String matriculationNumber) {
        return userService.getUser(matriculationNumber);
    }


    // post api----------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        userService.checkForTemporaryPassword(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PreAuthorize("hasPermission(null, 'User', 'update')")
    @PostMapping(path = "/user/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(new MessageResponse("User password changed!"));
    }
    @PreAuthorize("hasPermission(#updateUserRequest.matriculationNumber, 'User', 'update')")
    @PostMapping(path = "/user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        userService.updateUser(updateUserRequest);
        return ResponseEntity.ok(new MessageResponse("User was successfully updated!"));
    }

    // put api---------------------------------------------------------------------

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        userService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping(value = "/users")
    public ResponseEntity<RegisterMultipleUserResponse> registerUsers(@Valid @RequestParam(value = "file",required = true) MultipartFile file, @RequestParam(value = "isAdmin",required = false) Boolean isAdmin) throws UserException {
        return ResponseEntity.ok(userService.registerUsers(file, isAdmin));
    }

    // delete api -------------------------------------------------------------------------
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping(path = "/user/{matriculationNumber}")
    public ResponseEntity<?> deleteUser(@PathVariable String matriculationNumber) {
        userService.deleteUser(matriculationNumber);
        return ResponseEntity.ok(new MessageResponse("User was deleted!"));
    }
}
