package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record SimulationRemediation(
		List<Long> reviewDocumentIds,
		List<Long> retryScenarioIds,
		List<Long> compareOptionIds,
		List<String> missedDecisionCriteria
) {

	public SimulationRemediation {
		reviewDocumentIds = reviewDocumentIds == null ? List.of() : List.copyOf(reviewDocumentIds);
		retryScenarioIds = retryScenarioIds == null ? List.of() : List.copyOf(retryScenarioIds);
		compareOptionIds = compareOptionIds == null ? List.of() : List.copyOf(compareOptionIds);
		missedDecisionCriteria = missedDecisionCriteria == null ? List.of() : List.copyOf(missedDecisionCriteria);
	}
}
