package com.yunhwan.cloudsimlab.learningrelation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class LearningRelationsTests {

	@Test
	void 시나리오와_문서의_명시적_관계는_양방향에서_같은_이유를_사용한다() {
		LearningRelation scenarioRelation = LearningRelations.forScenario("single-spring-boot").stream()
				.filter(relation -> relation.documentKey().equals("ec2-compute-capacity"))
				.findFirst()
				.orElseThrow();
		LearningRelation documentRelation = LearningRelations.forDocument("ec2-compute-capacity").stream()
				.filter(relation -> relation.scenarioKey().equals("single-spring-boot"))
				.findFirst()
				.orElseThrow();

		assertThat(documentRelation).isEqualTo(scenarioRelation);
		assertThat(scenarioRelation.learningReason()).isNotBlank();
		assertThat(scenarioRelation.reviewFocus()).isNotBlank();
	}

	@Test
	void 정의되지_않은_키는_빈_관계를_반환한다() {
		assertThat(LearningRelations.forScenario("unknown-scenario")).isEmpty();
		assertThat(LearningRelations.forDocument("unknown-document")).isEmpty();
	}

	@Test
	void null과_공백_키는_빈_관계를_반환한다() {
		assertThat(LearningRelations.forScenario(null)).isEmpty();
		assertThat(LearningRelations.forScenario(" ")).isEmpty();
		assertThat(LearningRelations.forDocument(null)).isEmpty();
		assertThat(LearningRelations.forDocument(" ")).isEmpty();
	}

	@Test
	void 기존_시나리오와_문서는_각각_하나_이상의_명시적_관계를_가진다() {
		List<String> scenarioKeys = List.of(
				"single-spring-boot",
				"private-subnet-app",
				"traffic-spike-compute",
				"rds-failure",
				"read-heavy-performance"
		);
		List<String> documentKeys = List.of(
				"ec2-compute-capacity",
				"private-subnet-application-server",
				"alb-traffic-distribution",
				"auto-scaling-basics",
				"security-group-least-privilege",
				"rds-connection-management",
				"rds-multi-az",
				"read-replica-read-scaling",
				"redis-cache",
				"nat-gateway-outbound-communication"
		);

		assertThat(scenarioKeys).allSatisfy(key -> assertThat(LearningRelations.forScenario(key)).isNotEmpty());
		assertThat(documentKeys).allSatisfy(key -> assertThat(LearningRelations.forDocument(key)).isNotEmpty());
	}
}
