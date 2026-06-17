package com.yunhwan.cloudsimlab.learningdiscovery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.yunhwan.cloudsimlab.architecturepractice.application.port.ArchitecturePracticeQueryPort;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeConnection;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeLevel;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeNode;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryItem;
import com.yunhwan.cloudsimlab.learningdiscovery.domain.LearningDiscoveryResourceType;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningmodule.application.port.LearningModuleQueryPort;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

class LearningDiscoveryServiceTests {

	private final LearningDocumentQueryPort documentQueryPort = Mockito.mock(LearningDocumentQueryPort.class);
	private final ScenarioQueryPort scenarioQueryPort = Mockito.mock(ScenarioQueryPort.class);
	private final LearningModuleQueryPort moduleQueryPort = Mockito.mock(LearningModuleQueryPort.class);
	private final ArchitecturePracticeQueryPort architecturePracticeQueryPort = Mockito.mock(ArchitecturePracticeQueryPort.class);
	private final LearningDiscoveryService service = new LearningDiscoveryService(
			documentQueryPort,
			scenarioQueryPort,
			moduleQueryPort,
			architecturePracticeQueryPort
	);

	@Test
	void key가_없는_문서와_시나리오는_fallback_ID로_관계_맵을_일관되게_조회한다() {
		LearningDocument document = new LearningDocument(
				42L,
				null,
				"키 없는 문서",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"문서 요약",
				"문서 내용",
				2,
				List.of(),
				List.of("fallback-tag"),
				List.of(),
				List.of("7")
		);
		Scenario scenario = new Scenario(
				7L,
				null,
				"키 없는 시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"시나리오 요약",
				"시나리오 설명",
				List.of(),
				List.of(),
				List.of(),
				null,
				List.of(),
				List.of()
		);
		LearningModule module = new LearningModule(
				"single-server-deployment",
				"backend-aws-foundation",
				"단일 서버 배포",
				"설명",
				List.of("목표"),
				List.of(),
				1,
				List.of("42"),
				List.of("7"),
				List.of("fallback-practice")
		);
		ArchitecturePracticeTemplate practice = new ArchitecturePracticeTemplate(
				"fallback-practice",
				"Fallback 연습",
				"설명",
				ArchitecturePracticeLevel.BEGINNER,
				"학습 목표",
				List.of("지시"),
				List.of(new ArchitecturePracticeNode("ec2", UserArchitectureResourceType.EC2, "EC2")),
				List.of(new ArchitecturePracticeConnection("request", "ec2", "ec2", UserArchitectureConnectionType.REQUEST_FLOW)),
				List.of(UserArchitectureResourceType.EC2),
				List.of(UserArchitectureConnectionType.REQUEST_FLOW),
				List.of("42"),
				List.of("7"),
				List.of("single-server-deployment")
		);
		when(documentQueryPort.findAll()).thenReturn(List.of(document));
		when(scenarioQueryPort.findAll(null, null)).thenReturn(List.of(scenario));
		when(moduleQueryPort.findAll()).thenReturn(List.of(module));
		when(architecturePracticeQueryPort.findAll()).thenReturn(List.of(practice));

		List<LearningDiscoveryItem> result = service.findAll(null, null, null, null);

		LearningDiscoveryItem documentItem = item(result, LearningDiscoveryResourceType.DOCUMENT, "42");
		assertThat(documentItem.relatedModuleIds()).containsExactly("single-server-deployment");
		assertThat(documentItem.relatedScenarioIds()).containsExactly("7");
		assertThat(documentItem.relatedArchitecturePracticeIds()).containsExactly("fallback-practice");

		LearningDiscoveryItem scenarioItem = item(result, LearningDiscoveryResourceType.SCENARIO, "7");
		assertThat(scenarioItem.relatedDocumentIds()).containsExactly("42");
		assertThat(scenarioItem.relatedModuleIds()).containsExactly("single-server-deployment");
		assertThat(scenarioItem.relatedArchitecturePracticeIds()).containsExactly("fallback-practice");

		LearningDiscoveryItem moduleItem = item(result, LearningDiscoveryResourceType.MODULE, "single-server-deployment");
		assertThat(moduleItem.conceptTags()).containsExactly("fallback-tag");
	}

	private LearningDiscoveryItem item(List<LearningDiscoveryItem> items, LearningDiscoveryResourceType resourceType, String id) {
		return items.stream()
				.filter(item -> item.resourceType() == resourceType && item.id().equals(id))
				.findFirst()
				.orElseThrow();
	}
}
