package com.dodam.plan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "portone")
public class PlanPortoneProperties {
    private String apiKey;      // (선택) 필요시 사용
    private String secret;      // v2 Authorization: "PortOne {secret}"
    private String baseUrl;     // 기본값: https://api.portone.io
    private String storeId;     // (선택) 멀티스토어 사용 시
    private String confirmPath; // (선택) 기본 /payments/confirm
    private String pgName;      // (선택) 카드 메타에 넣을 PG명 표시용
    private String paymentGetPrefix; // 기본값 "/payments/"
}
