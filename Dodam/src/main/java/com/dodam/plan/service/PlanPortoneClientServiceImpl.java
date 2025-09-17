// src/main/java/com/dodam/plan/service/impl/PlanPortoneClientServiceImpl.java
package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.service.PlanPortoneClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PlanPortoneClientServiceImpl implements PlanPortoneClientService {

	  private final WebClient portoneWebClient;
	  private final String storeId;   // 환경설정에서 주입
	  private final boolean isTest;   // 개발용이면 true
	  private final ObjectMapper mapper = new ObjectMapper();

	  public PlanPortoneClientServiceImpl(
	      @Qualifier("portoneWebClient") WebClient portoneWebClient,
	      PlanPortoneProperties props // props.getStoreId(), props.isTest() 가정
	  ) {
	    this.portoneWebClient = portoneWebClient;
	    this.storeId = props.getStoreId();     // 없으면 null 허용
	    this.isTest = Boolean.TRUE.equals(props.getIsTest());
	  }

	  private static final ParameterizedTypeReference<Map<String,Object>> MAP =
	      new ParameterizedTypeReference<>() {};

	  @Override
	  public Map<String, Object> confirmIssueBillingKey(String billingIssueToken) {
	    Map<String, Object> body = new HashMap<>();
	    body.put("billingIssueToken", billingIssueToken);
	    if (storeId != null && !storeId.isBlank()) body.put("storeId", storeId); // 🔒 권한 상점 고정
	    body.put("isTest", isTest); // 테스트 환경이면 true

	    try {
	      return portoneWebClient.post()
	          .uri("/billing-keys/confirm") // ✅ v2 confirm
	          .contentType(MediaType.APPLICATION_JSON)
	          .bodyValue(body)
	          .retrieve()
	          .onStatus(HttpStatusCode::isError, resp ->
	              resp.bodyToMono(String.class).defaultIfEmpty("")
	                  .flatMap(err -> {
	                    // 401 원인 파악용 서버 로그
	                    log.error("[PortOne] confirm 4xx/5xx status={} body={}", resp.statusCode(), err);
	                    return resp.createException().flatMap(Mono::error);
	                  })
	          )
	          .bodyToMono(MAP)
	          .block(Duration.ofSeconds(30)); // v2 권장 타임아웃은 길게, 여기선 30s
	    } catch (RuntimeException e) {
	      log.error("[PortOne] confirm call failed: {}", e.toString());
	      throw e;
	    }
	  }
	  
}