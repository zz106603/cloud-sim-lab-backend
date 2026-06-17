package com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivityType;

class CurriculumSeedCatalogTests {

	@Test
	void 입문_경로는_설계서의_6단계_초보자_학습_흐름을_모듈로_표현한다() {
		assertThat(CurriculumSeedCatalog.paths()).hasSize(1);
		assertThat(CurriculumSeedCatalog.paths().get(0).moduleIds()).containsExactly(
				"single-server-deployment",
				"network-boundary",
				"alb-private-subnet",
				"auto-scaling-health-check",
				"data-tier-scaling",
				"user-architecture-practice"
		);
		assertThat(CurriculumSeedCatalog.modules())
				.extracting("id")
				.containsExactly(
						"single-server-deployment",
						"network-boundary",
						"alb-private-subnet",
						"auto-scaling-health-check",
						"data-tier-scaling",
						"user-architecture-practice"
				);
	}

	@Test
	void 문서와_시나리오의_역방향_모듈_ID를_계산한다() {
		assertThat(CurriculumSeedCatalog.moduleIdsForDocument("ec2-compute-capacity"))
				.containsExactly("single-server-deployment");
		assertThat(CurriculumSeedCatalog.moduleIdsForScenario("security-group-misconfiguration"))
				.containsExactly("network-boundary", "user-architecture-practice");
	}

	@Test
	void null_문서와_시나리오_키는_빈_모듈_ID_목록을_반환한다() {
		assertThat(CurriculumSeedCatalog.moduleIdsForDocument(null)).isEmpty();
		assertThat(CurriculumSeedCatalog.moduleIdsForScenario(null)).isEmpty();
	}

	@Test
	void 모듈_실습_활동은_읽기_실행_작성_순서로_안정적으로_생성된다() {
		assertThat(CurriculumSeedCatalog.modules().get(4).practiceActivities())
				.extracting("type", "targetResourceId", "recommendedOrder")
				.containsExactly(
						tuple(
								LearningModulePracticeActivityType.READ_DOCUMENT,
								"rds-connection-management",
								1
						),
						tuple(
								LearningModulePracticeActivityType.READ_DOCUMENT,
								"rds-multi-az",
								2
						),
						tuple(
								LearningModulePracticeActivityType.READ_DOCUMENT,
								"read-replica-read-scaling",
								3
						),
						tuple(
								LearningModulePracticeActivityType.READ_DOCUMENT,
								"redis-cache",
								4
						),
						tuple(
								LearningModulePracticeActivityType.RUN_SCENARIO,
								"rds-failure",
								5
						),
						tuple(
								LearningModulePracticeActivityType.RUN_SCENARIO,
								"read-heavy-performance",
								6
						),
						tuple(
								LearningModulePracticeActivityType.RUN_SCENARIO,
								"redis-failure-fallback",
								7
						),
						tuple(
								LearningModulePracticeActivityType.RUN_SCENARIO,
								"rds-connection-pool-exhaustion",
								8
						),
						tuple(
								LearningModulePracticeActivityType.BUILD_ARCHITECTURE,
								"read-heavy-scaling-practice",
								9
						)
				);
	}
}
