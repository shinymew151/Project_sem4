package com.ai.video.FacelessVideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class FacelessVideoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacelessVideoApplication.class, args);
	}

}
