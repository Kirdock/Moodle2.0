package com.aau.moodle20.security.services;

import com.aau.moodle20.domain.ECourseRole;
import com.aau.moodle20.domain.User;
import com.aau.moodle20.domain.UserInCourse;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
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


    public void registerUsers(MultipartFile file ) throws UserException
    {
        //TODO add validation
        List<User> users = new ArrayList<>();
        List<String> lines = readLinesFromFile(file);

        String password = encoder.encode("password");//TODO should not be hardcoded


        for (String line : lines)
        {
            String [] columns = line.split(";");
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


    public List<UserResponseObject> getUsersFromCourse(Long courseId ) throws UserException
    {
        //TODO add validation
        List<UserResponseObject> userResponseObjectList = new ArrayList<>();
        List<User> allUsers = userRepository.findByCourses_Course_Id(courseId);
        if(allUsers != null)
        {
            for(User user: allUsers)
            {
                UserResponseObject responseObject = new UserResponseObject();
                responseObject.setAdmin(user.getAdmin());
                responseObject.setForename(user.getForename());
                responseObject.setSurname(user.getSurname());
                responseObject.setMatrikelNummer(user.getMartikelNumber());
                responseObject.setUsername(user.getUsername());

                Optional<ECourseRole> role = user.getCourses().stream()
                        .filter(userInCourse -> courseId.equals(userInCourse.getCourse().getId()))
                        .map(UserInCourse::getRole)
                        .findFirst();
                if(role.isPresent())
                 responseObject.setRole(role.get());
                userResponseObjectList.add(responseObject);
            }
        }
        return userResponseObjectList;
    }

    protected List<String> readLinesFromFile(MultipartFile file) throws UserException
    {
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
            throw new UserException(e.getMessage(),e);
        }
        return lines;
    }
}
