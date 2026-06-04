package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

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

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int performanceEffect = 0;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int availabilityEffect = 0;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int costEffect = 0;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int complexityEffect = 0;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int consistencyEffect = 0;

	@Column(nullable = false, columnDefinition = "integer default 0")
	private int securityEffect = 0;

	protected JpaScenarioOptionEntity() {
	}

	private JpaScenarioOptionEntity(
			Long id,
			String graphKey,
			String name,
			String description,
			int score,
			boolean core,
			int riskScore,
			TradeOffEffects effects
	) {
		this.id = id;
		this.graphKey = graphKey;
		this.name = name;
		this.description = description;
		this.score = score;
		this.core = core;
		this.riskScore = riskScore;
		this.performanceEffect = effects.performance();
		this.availabilityEffect = effects.availability();
		this.costEffect = effects.cost();
		this.complexityEffect = effects.complexity();
		this.consistencyEffect = effects.consistency();
		this.securityEffect = effects.security();
	}

	static JpaScenarioOptionEntity from(ScenarioOption option) {
		return new JpaScenarioOptionEntity(
				option.getId(),
				option.getGraphKey(),
				option.getName(),
				option.getDescription(),
				option.getScore(),
				option.isCore(),
				option.getRiskScore(),
				option.getEffects()
		);
	}

	ScenarioOption toDomain() {
		return new ScenarioOption(
				id,
				graphKey,
				name,
				description,
				score,
				core,
				riskScore,
				new TradeOffEffects(
						performanceEffect,
						availabilityEffect,
						costEffect,
						complexityEffect,
						consistencyEffect,
						securityEffect
				)
		);
	}
}
