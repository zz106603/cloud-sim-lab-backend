package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public class SimulationResult {

	private final Long scenarioId;
	private final SimulationResultType resultType;
	private final int score;
	private final int riskScore;
	private final String summary;
	private final String detail;
	private final SimulationReview review;
	private final List<ScenarioOption> selectedOptions;
	private final TradeOffSummary tradeOffSummary;
	private final List<String> finalArchitecture;
	private final ArchitectureGraph finalArchitectureGraph;
	private final List<RelatedLearningDocument> relatedLearningDocuments;

	public SimulationResult(
			Long scenarioId,
			SimulationResultType resultType,
			int score,
			int riskScore,
			String summary,
			String detail,
			SimulationReview review,
			List<ScenarioOption> selectedOptions,
			TradeOffSummary tradeOffSummary,
			List<String> finalArchitecture,
			ArchitectureGraph finalArchitectureGraph,
			List<RelatedLearningDocument> relatedLearningDocuments
	) {
		this.scenarioId = scenarioId;
		this.resultType = resultType;
		this.score = score;
		this.riskScore = riskScore;
		this.summary = summary;
		this.detail = detail;
		this.review = review == null ? new SimulationReview("", List.of(), List.of(), List.of(), "") : review;
		this.selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
		this.tradeOffSummary = tradeOffSummary == null ? TradeOffSummary.from(this.selectedOptions) : tradeOffSummary;
		this.finalArchitecture = finalArchitecture == null ? List.of() : List.copyOf(finalArchitecture);
		this.finalArchitectureGraph = finalArchitectureGraph == null ? new ArchitectureGraph(List.of(), List.of()) : finalArchitectureGraph;
		this.relatedLearningDocuments = relatedLearningDocuments == null ? List.of() : List.copyOf(relatedLearningDocuments);
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

	public SimulationReview getReview() {
		return review;
	}

	public List<ScenarioOption> getSelectedOptions() {
		return selectedOptions;
	}

	public TradeOffSummary getTradeOffSummary() {
		return tradeOffSummary;
	}

	public List<String> getFinalArchitecture() {
		return finalArchitecture;
	}

	public ArchitectureGraph getFinalArchitectureGraph() {
		return finalArchitectureGraph;
	}

	public List<RelatedLearningDocument> getRelatedLearningDocuments() {
		return relatedLearningDocuments;
	}
}
