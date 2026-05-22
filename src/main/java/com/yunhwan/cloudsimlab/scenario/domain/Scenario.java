package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public class Scenario {

	private final Long id;
	private final String title;
	private final ScenarioCategory category;
	private final ScenarioLevel level;
	private final String summary;
	private final String description;
	private final List<ScenarioOption> options;

	public Scenario(
			Long id,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<ScenarioOption> options
	) {
		this.id = id;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.description = description;
		this.options = options == null ? List.of() : List.copyOf(options);
	}

	public static Scenario newScenario(
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<ScenarioOption> options
	) {
		return new Scenario(null, title, category, level, summary, description, options);
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public ScenarioCategory getCategory() {
		return category;
	}

	public ScenarioLevel getLevel() {
		return level;
	}

	public String getSummary() {
		return summary;
	}

	public String getDescription() {
		return description;
	}

	public List<ScenarioOption> getOptions() {
		return options;
	}
}
