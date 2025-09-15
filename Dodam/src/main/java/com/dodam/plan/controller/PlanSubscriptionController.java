package com.dodam.plan.controller;

import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import com.dodam.plan.service.PlanPaySubService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sub")
@RequiredArgsConstructor
public class PlanSubscriptionController {

  private final PlanPaySubService subSvc;

  public record StartSimpleReq(String planCode, Integer months) {}

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> start(@RequestBody StartSimpleReq req, HttpSession session) {
    String mid = (String) session.getAttribute("sid");
    if (!StringUtils.hasText(mid)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
    }
    if (req == null || !StringUtils.hasText(req.planCode())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_PLAN_CODE");
    }
    if (req.months() == null || req.months() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_MONTHS");
    }

    PmBillingMode mode = (req.months() == 1) ? PmBillingMode.MONTHLY : PmBillingMode.PREPAID_TERM;

    log.info("SUBSCRIBE start mid={}, planCode={}, months={}, mode={}", mid, req.planCode(), req.months(), mode);

    var result = subSvc.startByCodeAndMonths(mid, req.planCode(), req.months(), mode);

    return ResponseEntity.ok(Map.of(
        "pmId", result.pmId(),
        "invoiceId", result.invoiceId()
    ));
  }
}
