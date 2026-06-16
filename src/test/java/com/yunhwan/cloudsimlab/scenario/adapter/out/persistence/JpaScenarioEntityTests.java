package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

import jakarta.persistence.Column;

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

	@Test
	void fromPreservesScenarioLearningContext() {
		Scenario scenario = Scenario.newScenarioWithLearningContext(
				"single-spring-boot",
				"웹 서비스 확장",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"컴퓨팅 용량을 선택합니다.",
				"트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다.",
				List.of("Client", "EC2", "RDS"),
				List.of("single-server-deployment"),
				List.of(new ScenarioPrerequisiteConcept(
						"ec2-capacity",
						"EC2 용량",
						"ec2-compute-capacity",
						"EC2 용량 판단 기준이 필요합니다."
				)),
				new ScenarioObservationPoint(
						"EC2 CPU",
						"단일 EC2 장애",
						"Client -> EC2 -> RDS",
						"EC2 직접 노출",
						"정합성 변화는 작습니다.",
						"성능과 비용을 함께 봅니다."
				),
				List.of("performance", "availability"),
				List.of(ScenarioOption.newOption("선택지", "설명", 1, true, 0))
		);

		Scenario mapped = JpaScenarioEntity.from(scenario).toDomain();

		assertThat(mapped.getRelatedModuleIds()).containsExactly("single-server-deployment");
		assertThat(mapped.getPrerequisiteConcepts()).hasSize(1);
		assertThat(mapped.getObservationPoint().bottleneckMetric()).isEqualTo("EC2 CPU");
		assertThat(mapped.getJudgmentPerspectives()).containsExactly("performance", "availability");
	}

	@Test
	void observationPointColumnsAreRequiredWhenObservationPointExists() throws Exception {
		assertRequiredColumn("bottleneckMetric");
		assertRequiredColumn("failurePoint");
		assertRequiredColumn("requestFlow");
		assertRequiredColumn("securityBoundary");
		assertRequiredColumn("consistencyRisk");
		assertRequiredColumn("tradeOffSignal");
	}

	private void assertRequiredColumn(String fieldName) throws Exception {
		Field field = JpaScenarioObservationPointValue.class.getDeclaredField(fieldName);

		assertThat(field.getAnnotation(Column.class).nullable()).isFalse();
	}
}
