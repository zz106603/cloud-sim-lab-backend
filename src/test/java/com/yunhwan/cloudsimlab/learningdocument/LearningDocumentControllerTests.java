package com.yunhwan.cloudsimlab.learningdocument;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioSeedPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LearningDocumentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LearningDocumentSeedPort seedPort;

	@Autowired
	private ScenarioSeedPort scenarioSeedPort;

	private LearningDocument document;
	private Scenario scenario;

	@BeforeEach
	void setUp() {
		document = seedPort.save(LearningDocument.newDocumentWithKey(
				"ec2-compute-capacity",
				"Virtual machines and compute capacity",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"Understand compute capacity.",
				"Virtual machines run application workloads on configurable CPU and memory resources."
		));
		scenario = scenarioSeedPort.save(Scenario.newScenarioWithGraphKey(
				"single-spring-boot",
				"Scale a web service",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"Choose compute capacity.",
				"Compare compute choices before traffic increases.",
				List.of(),
				List.of(
						ScenarioOption.newOption("Large instance", "Higher capacity with higher cost.", 2, true, 0)
				)
		));
	}

	@Test
	void 문서_목록은_요약_정보만_반환한다() throws Exception {
		mockMvc.perform(get("/api/docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(document.getId()))
				.andExpect(jsonPath("$[0].title").value("Virtual machines and compute capacity"))
				.andExpect(jsonPath("$[0].category").value("COMPUTE"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].summary").value("Understand compute capacity."))
				.andExpect(jsonPath("$[0].content").doesNotExist());
	}

	@Test
	void 문서_상세는_본문과_관련_시나리오를_반환한다() throws Exception {
		mockMvc.perform(get("/api/docs/{documentId}", document.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(document.getId()))
				.andExpect(jsonPath("$.title").value("Virtual machines and compute capacity"))
				.andExpect(jsonPath("$.content").value("Virtual machines run application workloads on configurable CPU and memory resources."))
				.andExpect(jsonPath("$.relatedScenarios", hasSize(1)))
				.andExpect(jsonPath("$.relatedScenarios[0].id").value(scenario.getId()))
				.andExpect(jsonPath("$.relatedScenarios[0].title").value("Scale a web service"))
				.andExpect(jsonPath("$.relatedScenarios[0].reason").value(
						"단일 EC2의 용량 한계와 단일 장애 지점을 판단하는 데 필요한 문서입니다."
				));
	}

	@Test
	void 문서_상세는_같은_카테고리여도_명시적으로_연결되지_않은_시나리오를_제외한다() throws Exception {
		scenarioSeedPort.save(Scenario.newScenarioWithGraphKey(
				"unrelated-compute-scenario",
				"관련 없는 컴퓨팅 시나리오",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"다른 학습 목표를 확인합니다.",
				"문서와 명시적으로 연결되지 않은 시나리오입니다.",
				List.of(),
				List.of(ScenarioOption.newOption("선택지", "설명"))
		));

		mockMvc.perform(get("/api/docs/{documentId}", document.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.relatedScenarios", hasSize(1)))
				.andExpect(jsonPath("$.relatedScenarios[0].id").value(scenario.getId()));
	}

	@Test
	void 문서_상세는_명시적_연결_대상이_없으면_빈_관련_시나리오를_반환한다() throws Exception {
		LearningDocument securityDocument = seedPort.save(LearningDocument.newDocumentWithKey(
				"security-group-least-privilege",
				"Security Group 최소 허용",
				DocumentCategory.SECURITY,
				DocumentLevel.BEGINNER,
				"필요한 통신만 허용합니다.",
				"Security Group 본문"
		));

		mockMvc.perform(get("/api/docs/{documentId}", securityDocument.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.relatedScenarios", hasSize(0)));
	}

	@Test
	void 존재하지_않는_문서_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/docs/{documentId}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("LEARNING_DOCUMENT_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Learning document not found: 999"));
	}

	@Test
	void 잘못된_문서_ID_형식은_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/docs/{documentId}", "invalid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Invalid request value: documentId"));
	}
}
