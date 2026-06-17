package com.yunhwan.cloudsimlab.scenario.domain;

public record SimulationReflectionQuestion(
		String id,
		String question,
		Long relatedOptionId,
		String relatedTradeOffPerspective
) {
}
