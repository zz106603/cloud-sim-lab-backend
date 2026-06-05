package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public record TradeOffSummary(
		int performance,
		int availability,
		int cost,
		int complexity,
		int consistency,
		int security
) {

	public static TradeOffSummary from(List<ScenarioOption> options) {
		List<ScenarioOption> selectedOptions = options == null ? List.of() : options;
		int performance = 0;
		int availability = 0;
		int cost = 0;
		int complexity = 0;
		int consistency = 0;
		int security = 0;

		for (ScenarioOption option : selectedOptions) {
			TradeOffEffects effects = option.getEffects();
			performance += effects.performance();
			availability += effects.availability();
			cost += effects.cost();
			complexity += effects.complexity();
			consistency += effects.consistency();
			security += effects.security();
		}

		return new TradeOffSummary(performance, availability, cost, complexity, consistency, security);
	}
}
