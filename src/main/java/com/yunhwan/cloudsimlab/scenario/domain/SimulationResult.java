package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public class SimulationResult {

	private final Long scenarioId;
	private final SimulationResultType resultType;
	private final int score;
	private final int riskScore;
	private final String summary;
	private final String detail;
	private final List<ScenarioOption> selectedOptions;

	public SimulationResult(
			Long scenarioId,
			SimulationResultType resultType,
			int score,
			int riskScore,
			String summary,
			String detail,
			List<ScenarioOption> selectedOptions
	) {
		this.scenarioId = scenarioId;
		this.resultType = resultType;
		this.score = score;
		this.riskScore = riskScore;
		this.summary = summary;
		this.detail = detail;
		this.selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
	}

	public Long getScenarioId() {
		return scenarioId;
	}

	public SimulationResultType getResultType() {
		return resultType;
	}

	public int getScore() {
		return score;
	}

	public int getRiskScore() {
		return riskScore;
	}

	public String getSummary() {
		return summary;
	}

	public String getDetail() {
		return detail;
	}

	public List<ScenarioOption> getSelectedOptions() {
		return selectedOptions;
	}
}
