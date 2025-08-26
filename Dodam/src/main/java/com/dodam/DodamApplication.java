package com.dodam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dodam 애플리케이션의 메인 진입점 클래스입니다.
 * Spring Boot 애플리케이션을 부트스트랩하고 실행하는 역할을 합니다.
 */
@SpringBootApplication
public class DodamApplication {

	/**
	 * 애플리케이션의 메인 메소드입니다.
	 * SpringApplication.run()을 호출하여 Spring 애플리케이션 컨텍스트를 초기화하고 실행합니다.
	 * @param args 커맨드 라인 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(DodamApplication.class, args);
	}

}

