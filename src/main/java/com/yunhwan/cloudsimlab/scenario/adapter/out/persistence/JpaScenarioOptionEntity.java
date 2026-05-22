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

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 500)
	private String description;

	protected JpaScenarioOptionEntity() {
	}

	private JpaScenarioOptionEntity(Long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	static JpaScenarioOptionEntity from(ScenarioOption option) {
		return new JpaScenarioOptionEntity(option.getId(), option.getName(), option.getDescription());
	}

	ScenarioOption toDomain() {
		return new ScenarioOption(id, name, description);
	}
}
