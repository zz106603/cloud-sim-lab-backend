package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record FailureImpactResult(
		List<FailureImpactEdge> recoveredEdges,
		FailureImpact remainingImpact,
		List<String> postActionNotes
) {

	public FailureImpactResult {
		recoveredEdges = recoveredEdges == null ? List.of() : List.copyOf(recoveredEdges);
		postActionNotes = postActionNotes == null ? List.of() : List.copyOf(postActionNotes);
	}
}
