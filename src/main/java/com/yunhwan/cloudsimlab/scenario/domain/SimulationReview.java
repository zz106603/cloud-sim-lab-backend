package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record SimulationReview(
		String reason,
		List<String> strengths,
		List<String> limitations,
		List<String> missedTradeOffs,
		String nextStep
) {
	public SimulationReview {
		strengths = strengths == null ? List.of() : List.copyOf(strengths);
		limitations = limitations == null ? List.of() : List.copyOf(limitations);
		missedTradeOffs = missedTradeOffs == null ? List.of() : List.copyOf(missedTradeOffs);
	}
}
