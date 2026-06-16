package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.Objects;

public record ScenarioObservationPoint(
		String bottleneckMetric,
		String failurePoint,
		String requestFlow,
		String securityBoundary,
		String consistencyRisk,
		String tradeOffSignal
) {

	public ScenarioObservationPoint {
		bottleneckMetric = requireText(bottleneckMetric, "bottleneckMetric");
		failurePoint = requireText(failurePoint, "failurePoint");
		requestFlow = requireText(requestFlow, "requestFlow");
		securityBoundary = requireText(securityBoundary, "securityBoundary");
		consistencyRisk = requireText(consistencyRisk, "consistencyRisk");
		tradeOffSignal = requireText(tradeOffSignal, "tradeOffSignal");
	}

	private static String requireText(String value, String fieldName) {
		if (!Objects.requireNonNull(value, fieldName + " must not be null").isBlank()) {
			return value;
		}
		throw new IllegalArgumentException(fieldName + " must not be blank");
	}
}
