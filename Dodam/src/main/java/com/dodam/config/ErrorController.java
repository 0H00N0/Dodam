package com.dodam.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 에러 페이지 처리를 위한 컨트롤러
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping
    public String handleError(HttpServletRequest request) {
        // 에러 처리 로직
        return "error/customError";
    }
}
