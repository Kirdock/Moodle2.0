package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.ExampleResponseObject;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl extends AbstractService implements UserDetailsService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Value("${adminMatriculationNumber}")
    private String adminMatriculationNumber;

    @Value("${studentEmailPostfix}")
    private String studentEmailPostFix;

    private Pattern matriculationPattern = Pattern.compile("^[0-9]{8}$");

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public void registerUser(SignUpRequest signUpRequest) throws ServiceValidationException
    {
        if (userRepository.existsByMatriculationNumber(signUpRequest.getMatriculationNumber())) {
           throw new ServiceValidationException("Error: User with this matriculationNumber already exists!", ApiErrorResponseCodes.MATRICULACTIONNUMBER_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ServiceValidationException("Error: User with this username already exists!",ApiErrorResponseCodes.USERNAME_ALREADY_EXISTS);
        }

        String password = "password";//TODO should not be hardcoded
        password = encoder.encode(password);

        //username, matrikelNumber, forename, surename, password, isAdmin
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setMatriculationNumber(signUpRequest.getMatriculationNumber());
        user.setForename(signUpRequest.getForename());
        user.setSurname(signUpRequest.getSurname());
        user.setPassword(password);
        user.setAdmin(signUpRequest.getIsAdmin()!=null?signUpRequest.getIsAdmin():Boolean.FALSE);
        user.setEmail(signUpRequest.getEmail());
         userRepository.save(user);
    }


    public RegisterMultipleUserResponse registerUsers(MultipartFile file, Boolean isAdmin) throws UserException {
        List<User> users = getUserObjectsFromFile(file);
        List<User> usersToBeSaves = new ArrayList<>();
        RegisterMultipleUserResponse registerMultipleUserResponse = new RegisterMultipleUserResponse();
        String password = encoder.encode("password");//TODO should not be hardcoded

        Integer lineNumber = 1;
        for (User user : users) {
            user.setPassword(password);
            if (isAdmin != null)
                user.setAdmin(isAdmin);

            Matcher matcher = matriculationPattern.matcher(user.getMatriculationNumber());

            boolean isValid = matcher.matches() &&
                    (!userRepository.existsByMatriculationNumber(user.getMatriculationNumber()) &&
                            !userRepository.existsByUsername(user.getUsername()));
            if (!isValid) {
                registerMultipleUserResponse.getFailedUsers().add(lineNumber);
            } else
                usersToBeSaves.add(user);

            lineNumber++;
        }

        if(!registerMultipleUserResponse.getFailedUsers().isEmpty())
        {
            registerMultipleUserResponse.setMessage("The users in following line numbers could not be registered");
        }else
            registerMultipleUserResponse.setMessage("All users could be registered");

        userRepository.saveAll(usersToBeSaves);

        return registerMultipleUserResponse;
    }

    protected List<User> getUserObjectsFromFile(MultipartFile file) {
        List<String> lines = readLinesFromFile(file);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            String[] columns = line.split(";");
            User user = new User();
            user.setUsername(columns[0]);
            user.setMatriculationNumber(columns[1]);
            user.setSurname(columns[2]);
            user.setForename(columns[3]);
            user.setAdmin(Boolean.FALSE);
            user.setEmail(user.getUsername() + "@" + studentEmailPostFix);

            users.add(user);
        }

        return users;
    }

    public List<User> registerMissingUsersFromFile(MultipartFile file)
    {
        registerUsers(file,Boolean.FALSE);

        userRepository.flush();
        List<User> users = getUserObjectsFromFile(file);
        List<User> realUsers = new ArrayList<>();
        for(User user: users)
        {
            if(userRepository.existsByMatriculationNumber(user.getMatriculationNumber()))
            {
                realUsers.add(user);
            }
        }
        return realUsers;
    }

    /**
     * returns all users. If user is assigned to course, add course role
     * also join with finishe example and returns all presented examples
     * @param courseId
     * @return
     * @throws ServiceValidationException
     */
    public List<UserResponseObject> getUsersWithCourseRoles(Long courseId) throws ServiceValidationException {
        List<UserResponseObject> userResponseObjectList = new ArrayList<>();

        readCourse(courseId);
        List<UserInCourse> userInCourses = userInCourseRepository.findByCourse_Id(courseId);
        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(User::getAdmin);

        for (User user : allUsers) {
            UserResponseObject responseObject =  user.createUserResponseObject();
            Optional<UserInCourse> optionalUserInCourse = userInCourses.stream()
                    .filter(userInCourse -> user.getMatriculationNumber().equals(userInCourse.getUser().getMatriculationNumber()))
                    .findFirst();
            if (optionalUserInCourse.isPresent()) {
                responseObject.setCourseRole(optionalUserInCourse.get().getRole());
                // add presented examples of user
                if(ECourseRole.Student.equals(optionalUserInCourse.get().getRole())) {
                    responseObject.setPresentedCount(getPresentedExamplesCount(user,optionalUserInCourse.get().getCourse()));
                }
            }
            else
                responseObject.setCourseRole(ECourseRole.None);
            userResponseObjectList.add(responseObject);
        }
        return userResponseObjectList;
    }

    protected Integer getPresentedExamplesCount(User user, Course course)
    {
        List<FinishesExample> presentedExamples = user.getFinishedExamples().stream()
                .filter(FinishesExample::getHasPresented)
                .collect(Collectors.toList());

        // filter example who related to given course
        List<FinishesExample> presentedExamplesInCourse = new ArrayList<>();
        for(FinishesExample finishesExample: presentedExamples)
        {
            Course finishedExampleCourse = finishesExample.getExample().getExerciseSheet().getCourse();
            if(finishedExampleCourse.getId().equals(course.getId()))
                presentedExamplesInCourse.add(finishesExample);
        }

        return presentedExamplesInCourse.size();
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

    public List<UserResponseObject> getAllUsers() throws UserException {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getMatriculationNumber))
                .map(User::createUserResponseObject)
                .collect(Collectors.toList());
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest) throws ServiceValidationException {
        User currentUser = getCurrentUser();
        if (!encoder.matches(changePasswordRequest.getOldPassword(), currentUser.getPassword()))
            throw new ServiceValidationException("Password for User not correct!");
        currentUser.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);
    }

    public void updateUser(UpdateUserRequest updateUserRequest) throws ServiceValidationException {
        String matriculationNumber = null;
        UserDetailsImpl userDetails = getUserDetails();
        if (updateUserRequest.getMatriculationNumber() != null && updateUserRequest.getMatriculationNumber().length() > 0) {
            matriculationNumber = updateUserRequest.getMatriculationNumber();
            if (!userRepository.existsByMatriculationNumber(matriculationNumber))
                throw new ServiceValidationException("Error: user with matriculationNumber:" + matriculationNumber + " does not exists", HttpStatus.NOT_FOUND);
        } else {
            matriculationNumber = userDetails.getMatriculationNumber();
        }
        if(!userDetails.getAdmin() && !userDetails.getMatriculationNumber().equals(matriculationNumber))
            throw new ServiceValidationException("Error: User is not admin and therefore not allowed to edit other users than himself ", HttpStatus.UNAUTHORIZED);

        if(adminMatriculationNumber.equals(matriculationNumber))
            throw new ServiceValidationException("Error: Root admin cannot be updated!");

        User user = readUser(matriculationNumber);
        user.setEmail(updateUserRequest.getEmail());
        user.setSurname(updateUserRequest.getSurname());
        user.setForename(updateUserRequest.getForename());
        if(userDetails.getAdmin() && updateUserRequest.getIsAdmin()!=null)
            user.setAdmin(updateUserRequest.getIsAdmin());

        userRepository.save(user);
    }

    public void deleteUser(String matriculationNumber) throws ServiceValidationException {
        User user = readUser(matriculationNumber);
        if (user.getMatriculationNumber().equals(adminMatriculationNumber))
            throw new ServiceValidationException("Super Admin user cannot be deleted!");

        userRepository.delete(user);
    }

    public Boolean isOwner() {
        UserDetailsImpl userDetails = getUserDetails();
        return courseRepository.existsByOwner_MatriculationNumber(userDetails.getMatriculationNumber());
    }

    public UserResponseObject getUser(String matriculationNumber) {
        return readUser(matriculationNumber).createUserResponseObject();
    }


}