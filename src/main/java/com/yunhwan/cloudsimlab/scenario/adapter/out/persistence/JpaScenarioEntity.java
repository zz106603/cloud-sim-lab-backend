package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario")
class JpaScenarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 80)
	private String graphKey;

	@Column(nullable = false, length = 120)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ScenarioCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ScenarioLevel level;

	@Column(nullable = false, length = 500)
	private String summary;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@ElementCollection
	@CollectionTable(name = "scenario_initial_architecture", joinColumns = @JoinColumn(name = "scenario_id"))
	@OrderColumn(name = "order_index")
	@Column(name = "node", nullable = false, length = 120)
	private List<String> initialArchitecture = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "scenario_related_module", joinColumns = @JoinColumn(name = "scenario_id"))
	@OrderColumn(name = "order_index")
	@Column(name = "module_id", nullable = false, length = 120)
	private List<String> relatedModuleIds = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "scenario_prerequisite_concept", joinColumns = @JoinColumn(name = "scenario_id"))
	@OrderColumn(name = "order_index")
	private List<JpaScenarioPrerequisiteConceptValue> prerequisiteConcepts = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "scenario_observation_point", joinColumns = @JoinColumn(name = "scenario_id"))
	private List<JpaScenarioObservationPointValue> observationPoints = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "scenario_judgment_perspective", joinColumns = @JoinColumn(name = "scenario_id"))
	@OrderColumn(name = "order_index")
	@Column(name = "perspective", nullable = false, length = 80)
	private List<String> judgmentPerspectives = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "scenario_id", nullable = false)
	private List<JpaScenarioOptionEntity> options = new ArrayList<>();

	protected JpaScenarioEntity() {
	}

	private JpaScenarioEntity(
			Long id,
			String graphKey,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<String> initialArchitecture,
			List<String> relatedModuleIds,
			List<JpaScenarioPrerequisiteConceptValue> prerequisiteConcepts,
			List<JpaScenarioObservationPointValue> observationPoints,
			List<String> judgmentPerspectives,
			List<JpaScenarioOptionEntity> options
	) {
		this.id = id;
		this.graphKey = graphKey;
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.description = description;
		this.initialArchitecture.addAll(initialArchitecture);
		this.relatedModuleIds.addAll(relatedModuleIds);
		this.prerequisiteConcepts.addAll(prerequisiteConcepts);
		this.observationPoints.addAll(observationPoints);
		this.judgmentPerspectives.addAll(judgmentPerspectives);
		this.options.addAll(options);
	}

	static JpaScenarioEntity from(Scenario scenario) {
		return new JpaScenarioEntity(
				scenario.getId(),
				scenario.getGraphKey(),
				scenario.getTitle(),
				scenario.getCategory(),
				scenario.getLevel(),
				scenario.getSummary(),
				scenario.getDescription(),
				scenario.getInitialArchitecture(),
				scenario.getRelatedModuleIds(),
				scenario.getPrerequisiteConcepts().stream()
						.map(JpaScenarioPrerequisiteConceptValue::from)
						.toList(),
				JpaScenarioObservationPointValue.from(scenario.getObservationPoint()),
				scenario.getJudgmentPerspectives(),
				scenario.getOptions().stream()
						.map(JpaScenarioOptionEntity::from)
						.toList()
		);
	}

	Scenario toDomain() {
		return new Scenario(
				id,
				graphKey,
				title,
				category,
				level,
				summary,
				description,
				initialArchitecture,
				relatedModuleIds,
				prerequisiteConcepts.stream()
						.map(JpaScenarioPrerequisiteConceptValue::toDomain)
						.toList(),
				observationPoints.isEmpty() ? null : observationPoints.getFirst().toDomain(),
				judgmentPerspectives,
				options.stream()
						.map(JpaScenarioOptionEntity::toDomain)
						.toList()
		);
	}
}
