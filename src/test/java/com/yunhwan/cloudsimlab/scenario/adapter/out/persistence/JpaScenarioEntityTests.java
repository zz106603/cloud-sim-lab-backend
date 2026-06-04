package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

class JpaScenarioEntityTests {

	@Test
	void fromPreservesScenarioAndOptionIds() {
		Scenario scenario = new Scenario(
				1L,
				"웹 서비스 확장",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"컴퓨팅 용량을 선택합니다.",
				"트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다.",
				List.of("Client", "EC2", "RDS"),
				List.of(new ScenarioOption(
						10L,
						null,
						"작은 EC2 인스턴스 유지",
						"비용은 낮지만 용량이 제한적입니다.",
						1,
						false,
						0,
						new TradeOffEffects(0, -2, 2, 2, 0, 0)
				))
		);

		Scenario mapped = JpaScenarioEntity.from(scenario).toDomain();

		assertThat(mapped.getId()).isEqualTo(1L);
		assertThat(mapped.getInitialArchitecture()).containsExactly("Client", "EC2", "RDS");
		assertThat(mapped.getOptions()).extracting(ScenarioOption::getId).containsExactly(10L);
		assertThat(mapped.getOptions().getFirst().getEffects())
				.isEqualTo(new TradeOffEffects(0, -2, 2, 2, 0, 0));
	}
}
