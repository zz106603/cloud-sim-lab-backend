package com.yunhwan.cloudsimlab.scenario.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScenarioTests {

	@Test
	void constructorTreatsNullOptionsAsEmptyList() {
		Scenario scenario = new Scenario(
				1L,
				"웹 서비스 확장",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"컴퓨팅 용량을 선택합니다.",
				"트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다.",
				null
		);

		assertThat(scenario.getOptions()).isEmpty();
	}
}
