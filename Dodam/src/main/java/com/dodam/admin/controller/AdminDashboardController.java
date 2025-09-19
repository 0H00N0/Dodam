// com.dodam.admin.controller.AdminDashboardController.java

package com.dodam.admin.controller;

import com.dodam.admin.board.AdminNoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminNoticeService noticeService;

    @GetMapping({"/admin", "/admin/main"})
    public String dashboard(Model model) throws Exception {

        // 1) 최신 공지 4건
        model.addAttribute("latestNotices", noticeService.latest(4));

        // 2) 차트용 더미/샘플 데이터 (실서비스에서는 각 Service에서 값 가져오면 됨)
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("product", Map.of(
                "labels", List.of("유아용", "블록", "퍼즐", "교육"),
                "values", List.of(120, 80, 60, 40)
        ));
        chartData.put("member", Map.of(
                "labels", List.of("무료", "라이트", "스탠다드", "프리미엄"),
                "values", List.of(300, 180, 120, 40)
        ));
        chartData.put("rental", Map.of(
                "labels", List.of("대여", "반납"),
                "values", List.of(240, 220)
        ));
        chartData.put("payment", Map.of(
                "labels", List.of("1월","2월","3월","4월","5월","6월"),
                "values", List.of(1200, 1500, 1100, 2100, 2600, 3000)
        ));
        chartData.put("logistics", Map.of(
                "labels", List.of("집하", "이동중", "배송중", "완료"),
                "values", List.of(20, 40, 60, 200)
        ));
        chartData.put("voc", Map.of(
                "labels", List.of("칭찬","불만","제안","문의"),
                "values", List.of(45, 18, 22, 63)
        ));

        ObjectMapper om = new ObjectMapper();
        model.addAttribute("chartDataJson", om.writeValueAsString(chartData));

        return "admin/main"; // 템플릿 경로: templates/admin/main.html
    }
}
