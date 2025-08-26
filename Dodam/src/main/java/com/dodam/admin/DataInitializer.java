package com.dodam.admin;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 초기 데이터를 설정하는 컴포넌트입니다.
 * CommandLineRunner를 구현하여 Spring Boot 애플리케이션이 시작된 후 특정 코드를 실행합니다.
 * 주로 개발 환경에서 기본 관리자 계정 등을 생성하는 데 사용됩니다.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;

    /**
     * 애플리케이션 시작 시 실행되는 메소드입니다.
     * 데이터베이스에 기본 관리자 및 배달기사 계정이 없는 경우 생성합니다.
     * ddl-auto가 create 또는 create-drop으로 설정된 경우 유용합니다.
     * @param args 커맨드 라인 인자
     * @throws Exception 실행 중 발생할 수 있는 예외
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataInitializer running...");
        // 'admin' 계정이 없는 경우 생성
        if (!adminService.existsByUsername("admin")) {
            System.out.println("Admin user 'admin' does not exist. Creating...");
            AdminEntity admin = AdminEntity.builder()
                    .username("admin")
                    .password("password") // AdminService에서 인코딩 처리
                    .name("관리자")
                    .email("admin@dodam.com")
                    .role(AdminEntity.AdminRole.ADMIN)
                    .enabled(true)
                    .build();
            adminService.createAdmin(admin);
            System.out.println(">>> Default ADMIN user created: username=admin, password=password");
        }

        // 'deliveryman' 계정이 없는 경우 생성
        if (!adminService.existsByUsername("deleveryman")) {
            System.out.println("Deliveryman user 'deleveryman' does not exist. Creating...");
            AdminEntity deliveryman = AdminEntity.builder()
                    .username("deleveryman")
                    .password("password") // AdminService에서 인코딩 처리
                    .name("배송기사")
                    .email("deleveryman@dodam.com")
                    .role(AdminEntity.AdminRole.DELIVERYMAN)
                    .enabled(true)
                    .build();
            adminService.createAdmin(deliveryman);
            System.out.println(">>> Default DELIVERYMAN user created: username=deleveryman, password=password");
        }
    }
}
