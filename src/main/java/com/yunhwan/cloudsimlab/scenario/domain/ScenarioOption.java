package com.yunhwan.cloudsimlab.scenario.domain;

public class ScenarioOption {

	private final Long id;
	private final String name;
	private final String description;

	public ScenarioOption(Long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public static ScenarioOption newOption(String name, String description) {
		return new ScenarioOption(null, name, description);
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
}
