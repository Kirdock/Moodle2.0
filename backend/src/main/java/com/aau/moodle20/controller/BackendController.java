package com.aau.moodle20.controller;

import com.aau.moodle20.domain.ERole;
import com.aau.moodle20.domain.Role;
import com.aau.moodle20.domain.User;
import com.aau.moodle20.exception.UserNotFoundException;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.response.JwtResponse;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.repository.RoleRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import com.aau.moodle20.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api")
public class BackendController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

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
        User user = new User(signUpRequest.getUsername(), signUpRequest.getMatrikelNumber(),signUpRequest.getForename(),signUpRequest.getSurename(),password,Boolean.FALSE);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PutMapping("/users")
    public ResponseEntity<?> registerUsers(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Create new user's account - no admin
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()),Boolean.FALSE);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


     @GetMapping(path = "/user")
     public List<User> getUsers() {
         return userRepository.findAll();
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
