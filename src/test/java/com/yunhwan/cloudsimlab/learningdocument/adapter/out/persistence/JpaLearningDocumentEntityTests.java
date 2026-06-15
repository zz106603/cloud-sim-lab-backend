package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

class JpaLearningDocumentEntityTests {

	@Test
	void 명시적_관계에_사용하는_문서_키를_영속성_매핑에서_유지한다() {
		LearningDocument document = LearningDocument.newDocumentWithMetadata(
				"ec2-compute-capacity",
				"EC2와 컴퓨팅 용량",
				DocumentCategory.EC2,
				DocumentLevel.BEGINNER,
				"EC2 용량 판단 기준을 이해합니다.",
				"문서 본문",
				1,
				List.of(),
				List.of("EC2", "capacity"),
				List.of("single-server-deployment"),
				List.of("single-spring-boot")
		);

		LearningDocument mapped = JpaLearningDocumentEntity.from(document).toDomain();

		assertThat(mapped.getDocumentKey()).isEqualTo("ec2-compute-capacity");
		assertThat(mapped.getCategory()).isEqualTo(DocumentCategory.EC2);
		assertThat(mapped.getOrderIndex()).isEqualTo(1);
		assertThat(mapped.getConceptTags()).containsExactly("EC2", "capacity");
		assertThat(mapped.getRelatedModuleIds()).containsExactly("single-server-deployment");
		assertThat(mapped.getRelatedScenarioIds()).containsExactly("single-spring-boot");
	}
}
