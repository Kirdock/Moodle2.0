package com.aau.moodle20;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoodleController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Moodle 2.0";
	}


}