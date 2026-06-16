package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class JpaScenarioObservationPointValue {

	@Column(nullable = false, length = 500)
	private String bottleneckMetric;

	@Column(nullable = false, length = 500)
	private String failurePoint;

	@Column(nullable = false, length = 500)
	private String requestFlow;

	@Column(nullable = false, length = 500)
	private String securityBoundary;

	@Column(nullable = false, length = 500)
	private String consistencyRisk;

	@Column(nullable = false, length = 500)
	private String tradeOffSignal;

	protected JpaScenarioObservationPointValue() {
	}

	private JpaScenarioObservationPointValue(
			String bottleneckMetric,
			String failurePoint,
			String requestFlow,
			String securityBoundary,
			String consistencyRisk,
			String tradeOffSignal
	) {
		this.bottleneckMetric = bottleneckMetric;
		this.failurePoint = failurePoint;
		this.requestFlow = requestFlow;
		this.securityBoundary = securityBoundary;
		this.consistencyRisk = consistencyRisk;
		this.tradeOffSignal = tradeOffSignal;
	}

	static List<JpaScenarioObservationPointValue> from(ScenarioObservationPoint observationPoint) {
		if (observationPoint == null) {
			return List.of();
		}
		return List.of(new JpaScenarioObservationPointValue(
				observationPoint.bottleneckMetric(),
				observationPoint.failurePoint(),
				observationPoint.requestFlow(),
				observationPoint.securityBoundary(),
				observationPoint.consistencyRisk(),
				observationPoint.tradeOffSignal()
		));
	}

	ScenarioObservationPoint toDomain() {
		return new ScenarioObservationPoint(
				bottleneckMetric,
				failurePoint,
				requestFlow,
				securityBoundary,
				consistencyRisk,
				tradeOffSignal
		);
	}
}
