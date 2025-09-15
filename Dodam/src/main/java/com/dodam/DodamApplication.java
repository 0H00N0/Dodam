package com.dodam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DodamApplication {

	public static void main(String[] args) {
		SpringApplication.run(DodamApplication.class, args);
	}

}
