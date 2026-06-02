package com.yunhwan.cloudsimlab.scenario.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

	@Test
	void simulationReviewTreatsNullFieldsAsEmptyValues() {
		SimulationReview review = new SimulationReview(null, null, null, null, null);

		assertThat(review.reason()).isEmpty();
		assertThat(review.strengths()).isEmpty();
		assertThat(review.limitations()).isEmpty();
		assertThat(review.missedTradeOffs()).isEmpty();
		assertThat(review.nextStep()).isEmpty();
	}

	@Test
	void simulationReviewCopiesListFields() {
		List<String> strengths = new java.util.ArrayList<>();
		strengths.add("핵심 선택지를 포함합니다.");

		SimulationReview review = new SimulationReview("reason", strengths, null, null, "next");
		strengths.add("추가 변경");

		assertThat(review.strengths()).containsExactly("핵심 선택지를 포함합니다.");
	}
}
