package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario")
class JpaScenarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "scenario_id", nullable = false)
	private List<JpaScenarioOptionEntity> options = new ArrayList<>();

	protected JpaScenarioEntity() {
	}

	private JpaScenarioEntity(
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<JpaScenarioOptionEntity> options
	) {
		this.title = title;
		this.category = category;
		this.level = level;
		this.summary = summary;
		this.description = description;
		this.options.addAll(options);
	}

	static JpaScenarioEntity from(Scenario scenario) {
		return new JpaScenarioEntity(
				scenario.getTitle(),
				scenario.getCategory(),
				scenario.getLevel(),
				scenario.getSummary(),
				scenario.getDescription(),
				scenario.getOptions().stream()
						.map(JpaScenarioOptionEntity::from)
						.toList()
		);
	}

	Scenario toDomain() {
		return new Scenario(
				id,
				title,
				category,
				level,
				summary,
				description,
				options.stream()
						.map(JpaScenarioOptionEntity::toDomain)
						.toList()
		);
	}
}
