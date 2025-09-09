package com.dodam.admin.controller;

import com.dodam.admin.dto.AdminInfoResponse;
import com.dodam.admin.dto.AdminLoginRequest;
import com.dodam.admin.service.AdminAuthService;
import com.dodam.member.entity.MemberEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    public static final String ADMIN_SESSION_KEY = "adminUser";

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * POST /api/admin/auth/login
     * Handles admin login requests.
     */
    @PostMapping("/login")
    public ResponseEntity<AdminInfoResponse> login(@RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        // Authenticate the admin
        MemberEntity admin = adminAuthService.login(request);
        AdminInfoResponse adminInfo = new AdminInfoResponse(admin);

        // Create a new session or get the existing one
        HttpSession session = httpRequest.getSession(true);

        // Store admin information in the session
        session.setAttribute(ADMIN_SESSION_KEY, adminInfo);

        // Set session timeout (e.g., 30 minutes)
        session.setMaxInactiveInterval(1800);

        return ResponseEntity.ok(adminInfo);
    }

    /**
     * POST /api/admin/auth/logout
     * Handles admin logout requests.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Get the current session, but don't create a new one if it doesn't exist
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidate the session to log the user out
            session.invalidate();
        }
        return ResponseEntity.ok("Successfully logged out.");
    }

    /**
     * GET /api/admin/auth/check
     * A simple endpoint to check the current login status.
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkLoginStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(ADMIN_SESSION_KEY) != null) {
            return ResponseEntity.ok(session.getAttribute(ADMIN_SESSION_KEY));
        }
        return ResponseEntity.status(401).body("Not authenticated.");
    }
}
