package com.aau.moodle20;


import com.aau.moodle20.entity.FileType;
import com.aau.moodle20.entity.User;
import com.aau.moodle20.repository.FileTypeRepository;
import com.aau.moodle20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Controller
@SpringBootApplication
public class Application implements ApplicationRunner, ErrorController {

	@Autowired
	UserRepository userRepository;
	@Autowired
	FileTypeRepository fileTypeRepository;
	@Autowired
	PasswordEncoder encoder;



	private static final String PATH = "/error";

	@Value("${adminUserName}")
	private String adminUserName;
	@Value("${adminPassword}")
	private String adminPassword;
	@Value("${adminMatriculationNumber}")
	private String adminMatriculationNumber;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!userRepository.existsByUsername(adminUserName)) {
			userRepository.save(new User(adminUserName,adminMatriculationNumber,adminUserName, adminPassword, encoder.encode(adminPassword), Boolean.TRUE) );
		}
		if (!fileTypeRepository.existsByName("Word")) {
			fileTypeRepository.save(new FileType("Word", "application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
		}
		if (!fileTypeRepository.existsByName("Excel")) {
			fileTypeRepository.save(new FileType("Excel", "application/msexcel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
		}
		if (!fileTypeRepository.existsByName("Archiv-Dateien")) {
			fileTypeRepository.save(new FileType("Archiv-Dateien", "*.zip, *.rar"));
		}
	}



	@RequestMapping(value = PATH)
	public String error() {
		return "forward:/";
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}

}
