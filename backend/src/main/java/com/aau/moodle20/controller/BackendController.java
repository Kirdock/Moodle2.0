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

    public static final String HELLO_TEXT = "Hello from Spring Boot Backend!";
    public static final String SECURED_TEXT = "Hello from the secured resource!";

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

    @RequestMapping(path = "/hello")
    public String sayHello() {
        LOG.info("GET called on /hello resource");
        return HELLO_TEXT;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
       /* List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());*/
       List<String> roles = new ArrayList<>();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        /*if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }*/

       // user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


     @GetMapping(path = "/user/{id}")
     public User getUserById(@PathVariable("id") long id) {

         return userRepository.findById(id).map(user -> {
             LOG.info("Reading user with id " + id + " from database.");
            return user;
         }).orElseThrow(() -> new UserNotFoundException("The user with the id " + id + " couldn't be found in the database."));
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
