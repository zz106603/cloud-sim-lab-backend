package com.yunhwan.cloudsimlab.scenario.domain;

public class ScenarioOption {

	private final Long id;
	private final String name;
	private final String description;
	private final int score;
	private final boolean core;
	private final int riskScore;

	public ScenarioOption(Long id, String name, String description) {
		this(id, name, description, 1, false, 0);
	}

	public ScenarioOption(Long id, String name, String description, int score, boolean core, int riskScore) {
		this.id = id;
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

	public Long getId() {
		return id;
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
