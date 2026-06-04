package com.yunhwan.cloudsimlab.scenario.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.RelatedLearningDocument;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

class ScenarioServiceTests {

	private final ScenarioQueryPort scenarioQueryPort = Mockito.mock(ScenarioQueryPort.class);
	private final LearningDocumentQueryPort learningDocumentQueryPort = Mockito.mock(LearningDocumentQueryPort.class);
	private final ScenarioService service = new ScenarioService(scenarioQueryPort, learningDocumentQueryPort);

	@Test
	void 관련_학습_문서는_관계에_정의된_documentKey로만_조회한다() {
		List<String> documentKeys = List.of(
				"ec2-compute-capacity",
				"alb-traffic-distribution",
				"auto-scaling-basics"
		);
		when(learningDocumentQueryPort.findAllByDocumentKeyIn(documentKeys)).thenReturn(List.of(
				document("auto-scaling-basics"),
				document("ec2-compute-capacity"),
				document("alb-traffic-distribution")
		));

		List<RelatedLearningDocument> result = service.findRelatedLearningDocuments(scenario("single-spring-boot"));

		assertThat(result)
				.extracting(RelatedLearningDocument::title)
				.containsExactly(
						"ec2-compute-capacity",
						"alb-traffic-distribution",
						"auto-scaling-basics"
				);
		verify(learningDocumentQueryPort).findAllByDocumentKeyIn(documentKeys);
		verify(learningDocumentQueryPort, never()).findAll();
	}

	@Test
	void 관련_학습_문서_조회_결과에_중복_documentKey가_있으면_즉시_실패한다() {
		List<String> documentKeys = List.of(
				"ec2-compute-capacity",
				"alb-traffic-distribution",
				"auto-scaling-basics"
		);
		when(learningDocumentQueryPort.findAllByDocumentKeyIn(documentKeys)).thenReturn(List.of(
				document("ec2-compute-capacity"),
				document("ec2-compute-capacity")
		));

		assertThatThrownBy(() -> service.findRelatedLearningDocuments(scenario("single-spring-boot")))
				.isInstanceOf(IllegalStateException.class);
	}

	private LearningDocument document(String documentKey) {
		return LearningDocument.newDocumentWithKey(
				documentKey,
				documentKey,
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"요약",
				"내용"
		);
	}

	private Scenario scenario(String graphKey) {
		return Scenario.newScenarioWithGraphKey(
				graphKey,
				"시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"요약",
				"설명",
				List.of(),
				List.of()
		);
	}
}
