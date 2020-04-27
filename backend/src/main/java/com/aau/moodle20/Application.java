package com.aau.moodle20;

import com.aau.moodle20.domain.User;
import com.aau.moodle20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@SpringBootApplication
public class Application implements ApplicationRunner {

	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;

	@Value("${moodle20.app.adminUserName}")
	private String adminUserName;
	@Value("${moodle20.app.adminPassword}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!userRepository.existsByUsername(adminUserName)) {
			userRepository.save(new User(adminUserName, encoder.encode(adminPassword), Boolean.TRUE) );
		}
	}
}
