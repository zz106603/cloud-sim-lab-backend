package com.yunhwan.cloudsimlab.scenario.domain;

import java.util.List;

public class Scenario {

	private final Long id;
	private final String graphKey;
	private final String title;
	private final ScenarioCategory category;
	private final ScenarioLevel level;
	private final String summary;
	private final String description;
	private final List<String> initialArchitecture;
	private final List<String> relatedModuleIds;
	private final List<ScenarioPrerequisiteConcept> prerequisiteConcepts;
	private final ScenarioObservationPoint observationPoint;
	private final List<String> judgmentPerspectives;
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
		this(id, title, category, level, summary, description, List.of(), options);
	}

	public Scenario(
			Long id,
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<ScenarioOption> options
	) {
		this(id, graphKey, title, category, level, summary, description, initialArchitecture, List.of(), List.of(), null, List.of(), options);
	}

	public Scenario(
			Long id,
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<String> relatedModuleIds,
			List<ScenarioPrerequisiteConcept> prerequisiteConcepts,
			ScenarioObservationPoint observationPoint,
			List<String> judgmentPerspectives,
			List<ScenarioOption> options
	) {
		this.id = id;
		this.graphKey = graphKey;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.description = description;
		this.initialArchitecture = initialArchitecture == null ? List.of() : List.copyOf(initialArchitecture);
		this.relatedModuleIds = relatedModuleIds == null ? List.of() : List.copyOf(relatedModuleIds);
		this.prerequisiteConcepts = prerequisiteConcepts == null ? List.of() : List.copyOf(prerequisiteConcepts);
		this.observationPoint = observationPoint;
		this.judgmentPerspectives = judgmentPerspectives == null ? List.of() : List.copyOf(judgmentPerspectives);
		this.options = options == null ? List.of() : List.copyOf(options);
	}

	public Scenario(
			Long id,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<ScenarioOption> options
	) {
		this(id, null, title, category, level, summary, description, initialArchitecture, options);
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

	public static Scenario newScenario(
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<ScenarioOption> options
	) {
		return new Scenario(null, title, category, level, summary, description, initialArchitecture, options);
	}

	public static Scenario newScenarioWithGraphKey(
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<ScenarioOption> options
	) {
		return new Scenario(null, graphKey, title, category, level, summary, description, initialArchitecture, options);
	}

	public static Scenario newScenarioWithLearningContext(
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<String> relatedModuleIds,
			List<ScenarioPrerequisiteConcept> prerequisiteConcepts,
			ScenarioObservationPoint observationPoint,
			List<String> judgmentPerspectives,
			List<ScenarioOption> options
	) {
		return new Scenario(
				null,
				graphKey,
				title,
				category,
				level,
				summary,
				description,
				initialArchitecture,
				relatedModuleIds,
				prerequisiteConcepts,
				observationPoint,
				judgmentPerspectives,
				options
		);
	}

	public Long getId() {
		return id;
	}

	public String getGraphKey() {
		return graphKey;
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

	public List<String> getInitialArchitecture() {
		return initialArchitecture;
	}

	public List<String> getRelatedModuleIds() {
		return relatedModuleIds;
	}

	public List<ScenarioPrerequisiteConcept> getPrerequisiteConcepts() {
		return prerequisiteConcepts;
	}

	public ScenarioObservationPoint getObservationPoint() {
		return observationPoint;
	}

	public List<String> getJudgmentPerspectives() {
		return judgmentPerspectives;
	}

	public List<ScenarioOption> getOptions() {
		return options;
	}
}
