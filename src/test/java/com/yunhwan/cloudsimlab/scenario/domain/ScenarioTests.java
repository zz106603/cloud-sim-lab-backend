package com.yunhwan.cloudsimlab.scenario.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScenarioTests {

	@Test
	void constructorTreatsNullOptionsAsEmptyList() {
		Scenario scenario = new Scenario(
				1L,
				"Scale a web service",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"Choose compute capacity.",
				"Compare compute choices before traffic increases.",
				null
		);

		assertThat(scenario.getOptions()).isEmpty();
	}
}
