package com.yunhwan.cloudsimlab.learningdocument.application;

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
import com.yunhwan.cloudsimlab.learningdocument.domain.RelatedScenario;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

class LearningDocumentServiceTests {

	private final LearningDocumentQueryPort learningDocumentQueryPort = Mockito.mock(LearningDocumentQueryPort.class);
	private final ScenarioQueryPort scenarioQueryPort = Mockito.mock(ScenarioQueryPort.class);
	private final LearningDocumentService service = new LearningDocumentService(
			learningDocumentQueryPort,
			scenarioQueryPort
	);

	@Test
	void 관련_시나리오는_관계에_정의된_graphKey로만_조회한다() {
		LearningDocument document = document("ec2-compute-capacity");
		List<String> graphKeys = List.of("single-spring-boot", "traffic-spike-compute");
		when(scenarioQueryPort.findAllByGraphKeyIn(graphKeys)).thenReturn(List.of(
				scenario("traffic-spike-compute"),
				scenario("single-spring-boot")
		));

		List<RelatedScenario> result = service.findRelatedScenarios(document);

		assertThat(result)
				.extracting(relatedScenario -> relatedScenario.scenario().getGraphKey())
				.containsExactly("single-spring-boot", "traffic-spike-compute");
		verify(scenarioQueryPort).findAllByGraphKeyIn(graphKeys);
		verify(scenarioQueryPort, never()).findAll(null, null);
	}

	@Test
	void 관련_시나리오_조회_결과에_중복_graphKey가_있으면_즉시_실패한다() {
		List<String> graphKeys = List.of("single-spring-boot", "traffic-spike-compute");
		when(scenarioQueryPort.findAllByGraphKeyIn(graphKeys)).thenReturn(List.of(
				scenario("single-spring-boot"),
				scenario("single-spring-boot")
		));

		assertThatThrownBy(() -> service.findRelatedScenarios(document("ec2-compute-capacity")))
				.isInstanceOf(IllegalStateException.class);
	}

	private LearningDocument document(String documentKey) {
		return LearningDocument.newDocumentWithKey(
				documentKey,
				"학습 문서",
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
