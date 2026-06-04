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
		return new TradeOffSummary(
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::performance).sum(),
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::availability).sum(),
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::cost).sum(),
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::complexity).sum(),
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::consistency).sum(),
				selectedOptions.stream().map(ScenarioOption::getEffects).mapToInt(TradeOffEffects::security).sum()
		);
	}
}
