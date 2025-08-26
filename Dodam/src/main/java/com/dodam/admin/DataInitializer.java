package com.dodam.admin;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;

    @Override
    public void run(String... args) throws Exception {
        // ddl-auto가 create 또는 create-drop일 때 유용합니다.
        // 애플리케이션 시작 시 기본 관리자 및 기자 계정을 생성합니다.
        
        System.out.println("DataInitializer running...");
        if (!adminService.existsByUsername("admin")) {
            System.out.println("Admin user 'admin' does not exist. Creating...");
            AdminEntity admin = AdminEntity.builder()
                    .username("admin")
                    .password("password") // 실제 운영에서는 더 강력한 비밀번호를 사용해야 합니다.
                    .name("관리자")
                    .email("admin@dodam.com")
                    .role(AdminEntity.AdminRole.ADMIN)
                    .enabled(true)
                    .build();
            adminService.createAdmin(admin);
            System.out.println(">>> Default ADMIN user created: username=admin, password=password");
        }

        if (!adminService.existsByUsername("deleveryman")) {
            System.out.println("Deliveryman user 'deleveryman' does not exist. Creating...");
            AdminEntity deliveryman = AdminEntity.builder()
                    .username("deleveryman")
                    .password("password") // 실제 운영에서는 더 강력한 비밀번호를 사용해야 합니다.
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
