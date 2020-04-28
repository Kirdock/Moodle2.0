package com.aau.moodle20;

import com.aau.moodle20.domain.User;
import com.aau.moodle20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@SpringBootApplication
public class Application implements ApplicationRunner, ErrorController {

	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;

	private static final String PATH = "/error";

	@Value("${adminUserName}")
	private String adminUserName;
	@Value("${adminPassword}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!userRepository.existsByUsername(adminUserName)) {
			userRepository.save(new User(adminUserName,"123","admin", "admin", encoder.encode(adminPassword), Boolean.TRUE) );
		}
	}



	@RequestMapping(value = PATH)
	public String error() {
		return "redirect:/";
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}
}
