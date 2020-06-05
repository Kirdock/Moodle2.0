package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.exception.ServiceValidationException;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.FinishesExampleResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.repository.CourseRepository;
import com.aau.moodle20.repository.UserInCourseRepository;
import com.aau.moodle20.repository.UserRepository;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserInCourseRepository userInCourseRepository;

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


    public List<User> registerUsers(MultipartFile file) throws UserException {
        //TODO add validation
        List<User> users = new ArrayList<>();
        List<User> allGivenUsers = new ArrayList<>();
        List<String> lines = readLinesFromFile(file);

        String password = encoder.encode("password");//TODO should not be hardcoded


        for (String line : lines) {
            String[] columns = line.split(";");
            User user = new User();
            user.setUsername(columns[0]);
            user.setMatriculationNumber(columns[1]);
            user.setSurname(columns[2]);
            user.setForename(columns[3]);
            user.setAdmin(Boolean.FALSE);
            user.setPassword(password);
            user.setEmail(user.getUsername()+"@"+studentEmailPostFix);
            users.add(user);
        }

        allGivenUsers.addAll(users);

        // remove users which already exists
        users.removeIf(user -> userRepository.existsByMatriculationNumber(user.getMatriculationNumber()));
        userRepository.saveAll(users);

        return allGivenUsers;
    }

    /**
     * returns all users. If user is assigned to course, add course role
     * also join with finishe example and returns all presented examples
     * @param courseId
     * @return
     * @throws ServiceValidationException
     */
    public List<UserResponseObject> getUsersWithCourseRoles(Long courseId) throws ServiceValidationException {

        if(!courseRepository.existsById(courseId))
            throw new ServiceValidationException("Error: course not found",HttpStatus.NOT_FOUND);

        List<UserResponseObject> userResponseObjectList = new ArrayList<>();
        List<UserInCourse> userInCourses = userInCourseRepository.findByCourse_Id(courseId);
        List<User> allUsers = getAllUsers();
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

                    List<FinishesExampleResponse> presentedExampleResponses = getPresentedExamplesInCourse(user,optionalUserInCourse.get().getCourse());
                    responseObject.getPresentedExamples().addAll(presentedExampleResponses);
                }
            }
            else
                responseObject.setCourseRole(ECourseRole.None);
            userResponseObjectList.add(responseObject);
        }
        return userResponseObjectList;
    }

    protected List<FinishesExampleResponse> getPresentedExamplesInCourse(User user, Course course)
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

        // build response list
        List<FinishesExampleResponse> presentedExampleResponses = presentedExamplesInCourse.stream().
                map(FinishesExample::getFinishesExampleResponse).collect(Collectors.toList());

        return presentedExampleResponses;
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
        return userRepository.findAll();
    }

    public List<UserResponseObject> getAllUses() throws UserException
    {
        List<UserResponseObject> userResponseObjectList = getAllUsers().stream().map(User::createUserResponseObject).collect(Collectors.toList());
        return userResponseObjectList;
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest,String jwtToken)
    {
        String matriculationNumber = jwtUtils.getMatriculationNumberFromJwtToken(jwtToken.split(" ")[1].trim());

        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if(!encoder.matches(changePasswordRequest.getOldPassword(), optionalUser.get().getPassword()))
            throw new UserException("Password for User not correct!");

        User user = optionalUser.get();
        user.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
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



        User user = userRepository.findByMatriculationNumber(matriculationNumber).get();
        user.setEmail(updateUserRequest.getEmail());
        user.setSurname(updateUserRequest.getSurname());
        user.setForename(updateUserRequest.getForename());
        if(userDetails.getAdmin() && updateUserRequest.getIsAdmin()!=null)
            user.setAdmin(updateUserRequest.getIsAdmin());

        userRepository.save(user);
    }

    public void deleteUser(String matriculationNumber) throws ServiceValidationException
    {
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if(!optionalUser.isPresent())
            throw new ServiceValidationException("User not found",HttpStatus.NOT_FOUND);

        if(optionalUser.get().getMatriculationNumber().equals(adminMatriculationNumber))
            throw new ServiceValidationException("Super Admin user cannot be deleted!");

        userRepository.delete(optionalUser.get());
    }

    public Boolean isOwner() {
        UserDetailsImpl userDetails = getUserDetails();
        return courseRepository.existsByOwner_MatriculationNumber(userDetails.getMatriculationNumber());
    }

    public UserResponseObject getUser(String matriculationNumber)
    {
        Optional<User> optionalUser = userRepository.findByMatriculationNumber(matriculationNumber);
        if(!optionalUser.isPresent())
         throw new ServiceValidationException("Error: user with given matriculationNumber not found", HttpStatus.NOT_FOUND);
       return optionalUser.get().createUserResponseObject();
    }

    public UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}