package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

class JpaScenarioEntityTests {

	@Test
	void fromPreservesScenarioAndOptionIds() {
		Scenario scenario = new Scenario(
				1L,
				"Scale a web service",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"Choose compute capacity.",
				"Compare compute choices before traffic increases.",
				List.of(new ScenarioOption(10L, "Small instance", "Lower cost with limited capacity."))
		);

		Scenario mapped = JpaScenarioEntity.from(scenario).toDomain();

		assertThat(mapped.getId()).isEqualTo(1L);
		assertThat(mapped.getOptions()).extracting(ScenarioOption::getId).containsExactly(10L);
	}
}
