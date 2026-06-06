package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record FailureImpact(
		String failureSourceNodeId,
		List<String> affectedNodeIds,
		List<FailureImpactEdge> affectedEdges,
		List<String> userSymptoms,
		List<String> remainingRisks
) {

	public FailureImpact {
		affectedNodeIds = affectedNodeIds == null ? List.of() : List.copyOf(affectedNodeIds);
		affectedEdges = affectedEdges == null ? List.of() : List.copyOf(affectedEdges);
		userSymptoms = userSymptoms == null ? List.of() : List.copyOf(userSymptoms);
		remainingRisks = remainingRisks == null ? List.of() : List.copyOf(remainingRisks);
	}
}
