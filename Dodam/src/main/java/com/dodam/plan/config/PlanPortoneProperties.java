package com.dodam.plan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "portone") // application.properties의 portone.*
public class PlanPortoneProperties {
    private String baseUrl;
    private String v2Secret;
    private String storeId;
    private String paymentGetPrefix = "/payments/";
    private String pgName;
    private String channelKey;
    private String currency = "KRW";

    public String authHeader() { return "PortOne " + v2Secret; }
}
