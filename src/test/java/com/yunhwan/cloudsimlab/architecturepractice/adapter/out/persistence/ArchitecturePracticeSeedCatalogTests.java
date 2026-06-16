package com.yunhwan.cloudsimlab.architecturepractice.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ArchitecturePracticeSeedCatalogTests {

	@Test
	void 초기_아키텍처_연습_템플릿은_요구된_실습_주제를_포함한다() {
		assertThat(ArchitecturePracticeSeedCatalog.practices())
				.extracting("id")
				.containsExactly(
						"architecture-builder-basic",
						"alb-private-subnet-application",
						"read-heavy-scaling-practice"
				);
	}

	@Test
	void 템플릿_ID_카탈로그는_중복_없는_ID를_제공한다() {
		assertThat(ArchitecturePracticeSeedCatalog.practiceIds())
				.containsExactlyInAnyOrder(
						"architecture-builder-basic",
						"alb-private-subnet-application",
						"read-heavy-scaling-practice"
				);
	}
}
