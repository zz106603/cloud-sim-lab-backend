package com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
}
