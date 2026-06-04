package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

class JpaLearningDocumentEntityTests {

	@Test
	void 명시적_관계에_사용하는_문서_키를_영속성_매핑에서_유지한다() {
		LearningDocument document = LearningDocument.newDocumentWithKey(
				"ec2-compute-capacity",
				"EC2와 컴퓨팅 용량",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"EC2 용량 판단 기준을 이해합니다.",
				"문서 본문"
		);

		LearningDocument mapped = JpaLearningDocumentEntity.from(document).toDomain();

		assertThat(mapped.getDocumentKey()).isEqualTo("ec2-compute-capacity");
	}
}
