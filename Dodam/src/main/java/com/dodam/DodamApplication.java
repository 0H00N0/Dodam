package com.dodam;

import com.dodam.plan.config.PlanPortoneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.boot.context.properties.EnableConfigurationProperties;

=======
>>>>>>> refs/remotes/origin/chan787

@SpringBootApplication
<<<<<<< HEAD
@EnableConfigurationProperties(PlanPortoneProperties.class) // ✅ properties 빈 등록
=======
>>>>>>> refs/remotes/origin/chan787
public class DodamApplication {
    public static void main(String[] args) {
        SpringApplication.run(DodamApplication.class, args);
    }
}

