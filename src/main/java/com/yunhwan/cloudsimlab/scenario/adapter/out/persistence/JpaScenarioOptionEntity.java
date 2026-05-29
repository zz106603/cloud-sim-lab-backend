package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_option")
class JpaScenarioOptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 80)
	private String graphKey;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false, columnDefinition = "integer default 1")
	private int score = 1;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean core = false;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int riskScore = 0;

	protected JpaScenarioOptionEntity() {
	}

	private JpaScenarioOptionEntity(Long id, String graphKey, String name, String description, int score, boolean core, int riskScore) {
		this.id = id;
		this.graphKey = graphKey;
		this.name = name;
		this.description = description;
		this.score = score;
		this.core = core;
		this.riskScore = riskScore;
	}

	static JpaScenarioOptionEntity from(ScenarioOption option) {
		return new JpaScenarioOptionEntity(
				option.getId(),
				option.getGraphKey(),
				option.getName(),
				option.getDescription(),
				option.getScore(),
				option.isCore(),
				option.getRiskScore()
		);
	}

	ScenarioOption toDomain() {
		return new ScenarioOption(id, graphKey, name, description, score, core, riskScore);
	}
}
