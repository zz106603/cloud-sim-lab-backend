package com.yunhwan.cloudsimlab.scenario.domain;

public class ScenarioOption {

	private final Long id;
	private final String graphKey;
	private final String name;
	private final String description;
	private final int score;
	private final boolean core;
	private final int riskScore;

	public ScenarioOption(Long id, String name, String description) {
		this(id, name, description, 1, false, 0);
	}

	public ScenarioOption(Long id, String name, String description, int score, boolean core, int riskScore) {
		this(id, null, name, description, score, core, riskScore);
	}

	public ScenarioOption(Long id, String graphKey, String name, String description, int score, boolean core, int riskScore) {
		this.id = id;
		this.graphKey = graphKey;
		this.name = name;
		this.description = description;
		this.score = score;
		this.core = core;
		this.riskScore = riskScore;
	}

	public static ScenarioOption newOption(String name, String description) {
		return new ScenarioOption(null, name, description);
	}

	public static ScenarioOption newOption(String name, String description, int score, boolean core, int riskScore) {
		return new ScenarioOption(null, name, description, score, core, riskScore);
	}

	public static ScenarioOption newOptionWithGraphKey(
			String graphKey,
			String name,
			String description,
			int score,
			boolean core,
			int riskScore
	) {
		return new ScenarioOption(null, graphKey, name, description, score, core, riskScore);
	}

	public Long getId() {
		return id;
	}

	public String getGraphKey() {
		return graphKey;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getScore() {
		return score;
	}

	public boolean isCore() {
		return core;
	}

	public int getRiskScore() {
		return riskScore;
	}
}
