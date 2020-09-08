package com.aau.moodle20.services;

import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.FinishesExample;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.entity.UserInCourse;
import com.aau.moodle20.exception.ServiceException;
import com.aau.moodle20.exception.UserException;
import com.aau.moodle20.payload.request.ChangePasswordRequest;
import com.aau.moodle20.payload.request.LoginRequest;
import com.aau.moodle20.payload.request.SignUpRequest;
import com.aau.moodle20.payload.request.UpdateUserRequest;
import com.aau.moodle20.payload.response.FailedUserResponse;
import com.aau.moodle20.payload.response.RegisterMultipleUserResponse;
import com.aau.moodle20.payload.response.UserResponseObject;
import com.aau.moodle20.security.jwt.JwtUtils;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.passay.CharacterOccurrencesRule.ERROR_CODE;

@Service
public class UserService  extends AbstractService{


    private AuthenticationManager authenticationManager;
    private PasswordEncoder encoder;
    private JwtUtils jwtUtils;
    private EmailService emailService;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @Value("${adminMatriculationNumber}")
    private String adminMatriculationNumber;

    @Value("${developerMode}")
    private Boolean developerMode;

    @Value("${studentEmailPostfix}")
    private String studentEmailPostFix;

    @Value("${tempPasswordExpirationHours}")
    private Long tempPasswordExpirationHours;

    private Pattern matriculationPattern = Pattern.compile("^[0-9]{8}$");

    public UserService(AuthenticationManager authenticationManager, PasswordEncoder encoder, JwtUtils jwtUtils, EmailService emailService, ResourceBundleMessageSource resourceBundleMessageSource)
    {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
        this.emailService = emailService;
        this.resourceBundleMessageSource = resourceBundleMessageSource;
    }

    public void registerUser(SignUpRequest signUpRequest) throws ServiceException
    {
        if (userRepository.existsByMatriculationNumber(signUpRequest.getMatriculationNumber())) {
            throw new ServiceException("Error: User with this matriculationNumber already exists!", ApiErrorResponseCodes.MATRICULACTIONNUMBER_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ServiceException("Error: User with this username already exists!",ApiErrorResponseCodes.USERNAME_ALREADY_EXISTS);
        }

        String emailSubject = getLocaleMessage("registerUser.email.subject");
        String emailText = getLocaleMessage("registerUser.email.text");

        String password =  developerMode?"password":generateRandomPassword();
        String encodedPassword = encoder.encode(password);

        //username, matrikelNumber, forename, surename, password, isAdmin
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setMatriculationNumber(signUpRequest.getMatriculationNumber());
        user.setForename(signUpRequest.getForename());
        user.setSurname(signUpRequest.getSurname());
        user.setPassword(encodedPassword);
        user.setAdmin(signUpRequest.getIsAdmin() != null ? signUpRequest.getIsAdmin() : Boolean.FALSE);
        user.setEmail(signUpRequest.getEmail());
        if (!developerMode)
            user.setPasswordExpireDate(LocalDateTime.now().plusHours(tempPasswordExpirationHours));
        userRepository.save(user);
        if (!developerMode)
            emailService.sendEmail(user.getEmail(), emailSubject, emailText.replace("{password}", password));
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public RegisterMultipleUserResponse registerUsers(MultipartFile file, Boolean isAdmin) throws UserException {
        List<User> users = getUserObjectsFromFile(file);
        List<User> usersToBeSaves = new ArrayList<>();
        RegisterMultipleUserResponse registerMultipleUserResponse = new RegisterMultipleUserResponse();
        String standardPassword = encoder.encode("password");
        Map<String,String> passwords = new HashMap<>();

        String emailSubject = getLocaleMessage("registerUser.email.subject");
        String emailText = getLocaleMessage("registerUser.email.text");


        Integer lineNumber = 1;
        for (User user : users) {
            if (developerMode)
                user.setPassword(standardPassword);
            else {
                String password = generateRandomPassword();
                String encodedPassword = encoder.encode(password);
                user.setPassword(encodedPassword);
                user.setPasswordExpireDate(LocalDateTime.now().plusHours(tempPasswordExpirationHours));
                passwords.put(user.getMatriculationNumber(), password);
            }

            if (isAdmin != null)
                user.setAdmin(isAdmin);
            validateUserEntry(user,registerMultipleUserResponse,usersToBeSaves,lineNumber);
            lineNumber++;
        }
        userRepository.saveAll(usersToBeSaves);
        if (!developerMode) {
            for (User user : usersToBeSaves) {
                emailService.sendEmail(user.getEmail(), emailSubject, emailText.replace("{password}", passwords.get(user.getMatriculationNumber())));
            }
        }
        List<UserResponseObject> registeredUsers = usersToBeSaves.stream().map(User::createUserResponseObject).collect(Collectors.toList());
        registerMultipleUserResponse.getRegisteredUsers().addAll(registeredUsers);

        return registerMultipleUserResponse;
    }


    protected void validateUserEntry(User user, RegisterMultipleUserResponse registerMultipleUserResponse, List<User> userToBeSaved, Integer lineNumber) {
        Matcher matcher = matriculationPattern.matcher(user.getMatriculationNumber());

        boolean alreadyExists = userRepository.existsByMatriculationNumber(user.getMatriculationNumber()) &&
                userRepository.existsByUsername(user.getUsername());
        boolean userNameAlreadyExists = !userRepository.existsByMatriculationNumber(user.getMatriculationNumber()) &&
                userRepository.existsByUsername(user.getUsername());
        boolean matriculationNumberAlreadyExists = userRepository.existsByMatriculationNumber(user.getMatriculationNumber()) &&
                !userRepository.existsByUsername(user.getUsername());

        if (!matcher.matches()) {
            FailedUserResponse failedUserResponse = new FailedUserResponse();
            failedUserResponse.setMessage("Error: Wrong format for matriculationNumber");
            failedUserResponse.setLineNumber(lineNumber);
            failedUserResponse.setStatusCode(ApiErrorResponseCodes.REGISTER_USERS_WRONG_MATRICULATION_NUMBER_FORMAT);
            registerMultipleUserResponse.getFailedUsers().add(failedUserResponse);

        } else if (userNameAlreadyExists) {
            FailedUserResponse failedUserResponse = new FailedUserResponse();
            failedUserResponse.setMessage("Error: Username already exists");
            failedUserResponse.setLineNumber(lineNumber);
            failedUserResponse.setStatusCode(ApiErrorResponseCodes.REGISTER_USERS_USERNAME_ALREADY_EXISTS);
            registerMultipleUserResponse.getFailedUsers().add(failedUserResponse);
        } else if (matriculationNumberAlreadyExists) {
            FailedUserResponse failedUserResponse = new FailedUserResponse();
            failedUserResponse.setMessage("Error: Matriculation number already exists");
            failedUserResponse.setLineNumber(lineNumber);
            failedUserResponse.setStatusCode(ApiErrorResponseCodes.REGISTER_USERS_MATRICULATION_ALREADY_EXISTS);
            registerMultipleUserResponse.getFailedUsers().add(failedUserResponse);
        } else if(!alreadyExists)
            userToBeSaved.add(user);
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

    public List<User> registerMissingUsersFromFile(MultipartFile file, RegisterMultipleUserResponse registerMultipleUserResponse)
    {
        RegisterMultipleUserResponse registerMultipleUserResponse2 = registerUsers(file,Boolean.FALSE);
        registerMultipleUserResponse.setFailedUsers(registerMultipleUserResponse2.getFailedUsers());
        registerMultipleUserResponse.setRegisteredUsers(registerMultipleUserResponse2.getRegisteredUsers());

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
     * @throws ServiceException
     */
    public List<UserResponseObject> getUsersWithCourseRoles(Long courseId) throws ServiceException {
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

        userResponseObjectList.sort(Comparator.comparing(UserResponseObject::getMatriculationNumber));
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

    public void changePassword(ChangePasswordRequest changePasswordRequest) throws ServiceException {
        User currentUser = getCurrentUser();
        if (!encoder.matches(changePasswordRequest.getOldPassword(), currentUser.getPassword()))
            throw new ServiceException("Password for User not correct!");
        if(adminMatriculationNumber.equals(currentUser.getMatriculationNumber()))
            throw new ServiceException("Password for Root Admin cannot be changed");
        currentUser.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        currentUser.setPasswordExpireDate(null);
        userRepository.save(currentUser);
    }

    @Transactional
    public void updateUser(UpdateUserRequest updateUserRequest) throws ServiceException {
        String matriculationNumber = null;
        UserDetailsImpl userDetails = getUserDetails();
        if (updateUserRequest.getMatriculationNumber() != null && updateUserRequest.getMatriculationNumber().length() > 0) {
            matriculationNumber = updateUserRequest.getMatriculationNumber();
            if (!userRepository.existsByMatriculationNumber(matriculationNumber))
                throw new ServiceException("Error: user with matriculationNumber:" + matriculationNumber + " does not exists", HttpStatus.NOT_FOUND);
        } else {
            matriculationNumber = userDetails.getMatriculationNumber();
        }

        if(adminMatriculationNumber.equals(matriculationNumber))
            throw new ServiceException("Error: Root admin cannot be updated!");

        User user = readUser(matriculationNumber);
        boolean hasEmailChanged = !updateUserRequest.equals(user.getEmail());
        user.setEmail(updateUserRequest.getEmail());
        user.setSurname(updateUserRequest.getSurname());
        user.setForename(updateUserRequest.getForename());
        if(userDetails.getAdmin() && updateUserRequest.getIsAdmin()!=null)
            user.setAdmin(updateUserRequest.getIsAdmin());

        userRepository.saveAndFlush(user);
        if(hasEmailChanged && user.getPasswordExpireDate()!=null)
            generateNewTemporaryPassword(user);
    }


    @Transactional
    public void deleteUser(String matriculationNumber) throws ServiceException {
        User user = readUser(matriculationNumber);

        if (user.getMatriculationNumber().equals(adminMatriculationNumber))
            throw new ServiceException("Super Admin user cannot be deleted!");

        User adminUser = readUser(adminMatriculationNumber);
        List<Course> courses = courseRepository.findByOwner_MatriculationNumber(matriculationNumber);
        for(Course course : courses)
        {
            course.setOwner(adminUser);
        }

        courseRepository.saveAll(courses);
        userRepository.delete(user);
    }

    public Boolean isOwner() {
        UserDetailsImpl userDetails = getUserDetails();
        return courseRepository.existsByOwner_MatriculationNumber(userDetails.getMatriculationNumber());
    }

    public UserResponseObject getUser(String matriculationNumber) {

        return readUser(matriculationNumber).createUserResponseObject();
    }

    protected String generateRandomPassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        String password = gen.generatePassword(10, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
        return password;
    }


    public void checkForTemporaryPassword(LoginRequest loginRequest) throws ServiceException
    {
        Optional<User> optionalUser= userRepository.findByUsername(loginRequest.getUsername());
        if(optionalUser.isPresent()){
            if(optionalUser.get().getPasswordExpireDate()!=null)
            {
                LocalDateTime now = LocalDateTime.now();
                if(now.isAfter(optionalUser.get().getPasswordExpireDate())) {
                    generateNewTemporaryPassword(optionalUser.get());
                    throw new ServiceException("Error: temporary password is expired", ApiErrorResponseCodes.TEMPORARY_PASSWORD_EXPIRED);
                }else
                {
                    optionalUser.get().setPasswordExpireDate(null);
                    userRepository.save(optionalUser.get());
                }

            }
        }
    }

    protected void generateNewTemporaryPassword(User user)
    {
        String password = developerMode ? "password" : generateRandomPassword();
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);
        if (!developerMode)
            user.setPasswordExpireDate(LocalDateTime.now().plusHours(tempPasswordExpirationHours));
        userRepository.save(user);

        if(!developerMode) {
            String emailSubject = getLocaleMessage("registerUser.email.subject");
            String emailText = getLocaleMessage("registerUser.email.text");

            emailService.sendEmail(user.getEmail(), emailSubject, emailText.replace("{password}", password));
        }
    }

    private String getLocaleMessage(String code) {
        return resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
