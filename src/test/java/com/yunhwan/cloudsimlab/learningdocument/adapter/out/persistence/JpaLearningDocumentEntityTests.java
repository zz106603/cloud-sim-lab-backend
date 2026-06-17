package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentCheckpoint;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentRecallQuestion;

class JpaLearningDocumentEntityTests {

	@Test
	void 명시적_관계에_사용하는_문서_키를_영속성_매핑에서_유지한다() {
		LearningDocument document = LearningDocument.newDocumentWithReinforcement(
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
				List.of("single-spring-boot"),
				List.of(new LearningDocumentCheckpoint(
						"ec2-cp-capacity",
						"EC2 크기 증설은 성능과 비용을 함께 바꿉니다.",
						List.of("performance", "cost")
				)),
				List.of(new LearningDocumentRecallQuestion(
						"ec2-rq-single",
						"단일 EC2의 남는 위험은 무엇인가요?",
						"단일 장애 지점과 배포 중단 위험입니다.",
						"single-spring-boot"
				))
		);

		LearningDocument mapped = JpaLearningDocumentEntity.from(document).toDomain();

		assertThat(mapped.getDocumentKey()).isEqualTo("ec2-compute-capacity");
		assertThat(mapped.getCategory()).isEqualTo(DocumentCategory.EC2);
		assertThat(mapped.getOrderIndex()).isEqualTo(1);
		assertThat(mapped.getConceptTags()).containsExactly("EC2", "capacity");
		assertThat(mapped.getRelatedModuleIds()).containsExactly("single-server-deployment");
		assertThat(mapped.getRelatedScenarioIds()).containsExactly("single-spring-boot");
		assertThat(mapped.getCheckpoints()).hasSize(1);
		assertThat(mapped.getCheckpoints().get(0).judgmentPerspectives()).containsExactly("performance", "cost");
		assertThat(mapped.getRecallQuestions()).hasSize(1);
		assertThat(mapped.getRecallQuestions().get(0).relatedScenarioId()).isEqualTo("single-spring-boot");
	}
}
