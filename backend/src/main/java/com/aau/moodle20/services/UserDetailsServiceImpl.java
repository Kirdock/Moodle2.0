package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.domain.User;
import com.aau.moodle20.domain.UserInCourse;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.response.MessageResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.UserInCourseRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public void registerUser(SignUpRequest signUpRequest) throws UserException
    {
        if (userRepository.existsByMatrikelNummer(signUpRequest.getMatrikelnummer())) {
           throw new UserException("Error: User with this matrikelNummer already exists!");
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
    }


    public void registerUsers(MultipartFile file) throws UserException {
        //TODO add validation
        List<User> users = new ArrayList<>();
        List<String> lines = readLinesFromFile(file);

        String password = encoder.encode("password");//TODO should not be hardcoded


        for (String line : lines) {
            String[] columns = line.split(";");
            User user = new User();
            user.setUsername(columns[0]);
            user.setMartikelNumber(columns[1]);
            user.setSurname(columns[2]);
            user.setForename(columns[3]);
            user.setAdmin(Boolean.FALSE);
            user.setPassword(password);
            users.add(user);
        }

        // remove users which already exists
        users.removeIf(user -> userRepository.existsByMatrikelNummer(user.getMartikelNumber()));
        userRepository.saveAll(users);
    }


    public List<UserResponseObject> getUsersWithCourseRoles(Long courseId) throws UserException {
        //TODO add validation
        List<UserResponseObject> userResponseObjectList = new ArrayList<>();
        List<UserInCourse> userInCourses = userInCourseRepository.findByCourse_Id(courseId);
        List<User> allUser = getAllUsers();

        for (User user : allUser) {
            UserResponseObject responseObject = createResponseObject(user);

            Optional<ECourseRole> role = userInCourses.stream()
                    .filter(userInCourse -> user.getMartikelNumber().equals(userInCourse.getUser().getMartikelNumber()))
                    .map(UserInCourse::getRole)
                    .findFirst();
            if (role.isPresent())
                responseObject.setRole(role.get());
            else
                responseObject.setRole(ECourseRole.None);
            userResponseObjectList.add(responseObject);
        }

        return userResponseObjectList;
    }

    protected List<String> readLinesFromFile(MultipartFile file) throws UserException {
        BufferedReader br;
        List<String> lines = new ArrayList<>();
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            lines.remove(0); // Remove first line because it only contains column descriptions
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new UserException(e.getMessage(), e);
        }
        return lines;
    }

    /**
     * returns all users (except admin)
     * @return
     * @throws UserException
     */
    public List<User> getAllUsers() throws UserException {
        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(User::getAdmin);

        return allUsers;
    }

    public List<UserResponseObject> getAllUserResponseObjects() throws UserException
    {
        List<UserResponseObject> userResponseObjectList = new ArrayList<>();
        List<User> allUsers = getAllUsers();
        for(User user: allUsers)
            userResponseObjectList.add(createResponseObject(user));

        return userResponseObjectList;
    }

    /**
     * creates a user response object from the given user entity object
     * @param user
     * @return
     */
    protected UserResponseObject createResponseObject(User user)
    {
        UserResponseObject responseObject = new UserResponseObject();
        responseObject.setAdmin(user.getAdmin());
        responseObject.setForename(user.getForename());
        responseObject.setSurname(user.getSurname());
        responseObject.setMatrikelNummer(user.getMartikelNumber());
        responseObject.setUsername(user.getUsername());

        return responseObject;
    }
}