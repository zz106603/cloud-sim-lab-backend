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

	private Scenario computeScenario;

	@BeforeEach
	void setUp() {
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
	void findAllReturnsScenarioSummaries() throws Exception {
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
	void findAllFiltersByCategoryAndLevel() throws Exception {
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
	void findOneReturnsScenarioDetailWithOptions() throws Exception {
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
	void findOneReturnsNotFoundForMissingScenario() throws Exception {
		mockMvc.perform(get("/api/scenarios/{scenarioId}", 999L))
				.andExpect(status().isNotFound());
	}

	@Test
	void simulateReturnsGoodResultWhenCoreOptionIsSelected() throws Exception {
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
				.andExpect(jsonPath("$.selectedOptions[0].id").value(coreOptionId));
	}

	@Test
	void simulateReturnsNotFoundForMissingScenario() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", 999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[1]}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void simulateReturnsBadRequestForUnknownOptionId() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[999]}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void simulateReturnsBadRequestForEmptySelectedOptionIds() throws Exception {
		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[]}
								"""))
				.andExpect(status().isBadRequest());
	}
}
