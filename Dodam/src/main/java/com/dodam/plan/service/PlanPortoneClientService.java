// src/main/java/com/dodam/plan/service/PortoneClient.java
package com.dodam.plan.service;

import java.util.Map;

public interface PlanPortoneClientService {
	Map<String, Object> confirmIssueBillingKey(String billingIssueToken);
}
