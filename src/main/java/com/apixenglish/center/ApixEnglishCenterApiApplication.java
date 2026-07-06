package com.apixenglish.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApixEnglishCenterApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApixEnglishCenterApiApplication.class, args);
	}

}
