package com.yunhwan.cloudsimlab.scenario;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class ScenarioControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ScenarioSeedPort seedPort;

	@Autowired
	private LearningDocumentSeedPort learningDocumentSeedPort;

	private Scenario computeScenario;
	private LearningDocument computeDocument;

	@BeforeEach
	void setUp() {
		computeDocument = learningDocumentSeedPort.save(LearningDocument.newDocument(
				"Virtual machines and compute capacity",
				DocumentCategory.COMPUTE,
				DocumentLevel.BEGINNER,
				"Understand compute capacity.",
				"Virtual machines run application workloads on configurable CPU and memory resources."
		));
		computeScenario = seedPort.save(Scenario.newScenario(
				"Scale a web service",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"Choose compute capacity.",
				"Compare compute choices before traffic increases.",
				List.of(
						ScenarioOption.newOption("Small instance", "Lower cost with limited capacity.", 1, false, 0),
						ScenarioOption.newOption("Large instance", "Higher capacity with higher cost.", 2, true, 0)
				)
		));
		seedPort.save(Scenario.newScenario(
				"Separate public and private traffic",
				ScenarioCategory.NETWORK,
				ScenarioLevel.INTERMEDIATE,
				"Design basic network boundaries.",
				"Decide how to expose only the required network surface.",
				List.of(
						ScenarioOption.newOption("Public subnet", "Expose resources to inbound internet traffic.", 1, false, 2)
				)
		));
	}

	@Test
	void 시나리오_목록은_요약_정보만_반환한다() throws Exception {
		mockMvc.perform(get("/api/scenarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(computeScenario.getId()))
				.andExpect(jsonPath("$[0].title").value("Scale a web service"))
				.andExpect(jsonPath("$[0].category").value("COMPUTE"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].summary").value("Choose compute capacity."))
				.andExpect(jsonPath("$[0].description").doesNotExist())
				.andExpect(jsonPath("$[0].options").doesNotExist());
	}

	@Test
	void 시나리오_목록은_카테고리와_난이도로_필터링된다() throws Exception {
		mockMvc.perform(get("/api/scenarios")
						.param("category", "COMPUTE")
						.param("level", "BEGINNER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(computeScenario.getId()))
				.andExpect(jsonPath("$[0].category").value("COMPUTE"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"));
	}

	@Test
	void 시나리오_상세는_설명과_선택지를_반환한다() throws Exception {
		mockMvc.perform(get("/api/scenarios/{scenarioId}", computeScenario.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(computeScenario.getId()))
				.andExpect(jsonPath("$.title").value("Scale a web service"))
				.andExpect(jsonPath("$.description").value("Compare compute choices before traffic increases."))
				.andExpect(jsonPath("$.options", hasSize(2)))
				.andExpect(jsonPath("$.options[0].name").value("Small instance"))
				.andExpect(jsonPath("$.options[0].description").value("Lower cost with limited capacity."));
	}

	@Test
	void 존재하지_않는_시나리오_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/scenarios/{scenarioId}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("SCENARIO_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Scenario not found: 999"));
	}

	@Test
	void 잘못된_시나리오_ID_형식은_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/scenarios/{scenarioId}", "invalid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Invalid request value: scenarioId"));
	}

	@Test
	void 핵심_선택지를_고르면_GOOD_시뮬레이션_결과와_관련_문서를_반환한다() throws Exception {
		Long coreOptionId = computeScenario.getOptions().get(1).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(coreOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scenarioId").value(computeScenario.getId()))
				.andExpect(jsonPath("$.resultType").value("GOOD"))
				.andExpect(jsonPath("$.score").value(2))
				.andExpect(jsonPath("$.riskScore").value(0))
				.andExpect(jsonPath("$.summary").value("The selected options address the scenario well."))
				.andExpect(jsonPath("$.detail").exists())
				.andExpect(jsonPath("$.selectedOptions", hasSize(1)))
				.andExpect(jsonPath("$.selectedOptions[0].id").value(coreOptionId))
				.andExpect(jsonPath("$.relatedLearningDocuments", hasSize(1)))
				.andExpect(jsonPath("$.relatedLearningDocuments[0].id").value(computeDocument.getId()))
				.andExpect(jsonPath("$.relatedLearningDocuments[0].title").value("Virtual machines and compute capacity"));
	}

	@Test
	void 핵심_선택지가_없는_시나리오는_유효한_선택지만으로_GOOD_결과를_반환한다() throws Exception {
		Scenario noCoreScenario = seedPort.save(Scenario.newScenario(
				"Tune cache capacity",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"Choose cache settings.",
				"Select a useful cache option.",
				List.of(
						ScenarioOption.newOption("Increase cache size", "Reduce repeated reads.", 2, false, 0)
				)
		));
		Long optionId = noCoreScenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", noCoreScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(optionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("GOOD"));
	}

	@Test
	void 시뮬레이션은_중복_선택지를_제거하고_선택_순서를_유지한다() throws Exception {
		Long firstSelectedOptionId = computeScenario.getOptions().get(1).getId();
		Long secondSelectedOptionId = computeScenario.getOptions().get(0).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d,%d,%d]}
								""".formatted(firstSelectedOptionId, secondSelectedOptionId, firstSelectedOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.selectedOptions", hasSize(2)))
				.andExpect(jsonPath("$.selectedOptions[0].id").value(firstSelectedOptionId))
				.andExpect(jsonPath("$.selectedOptions[1].id").value(secondSelectedOptionId));
	}

	@Test
	void 존재하지_않는_시나리오_시뮬레이션은_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", 999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[1]}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("SCENARIO_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Scenario not found: 999"));
	}

	@Test
	void 존재하지_않는_선택지_ID는_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[999]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_SIMULATION_REQUEST"))
				.andExpect(jsonPath("$.message").value("Unknown scenario option IDs: [999]"));
	}

	@Test
	void 빈_선택지_목록은_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_SIMULATION_REQUEST"))
				.andExpect(jsonPath("$.message").value("selectedOptionIds must not be empty"));
	}

	@Test
	void 잘못된_JSON_본문은_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[1]
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
	}

	@Test
	void 선택지_ID_타입이_잘못되면_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":["invalid"]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
	}

	@Test
	void 잘못된_시뮬레이션_시나리오_ID_형식은_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", "invalid")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[1]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Invalid request value: scenarioId"));
	}
}
