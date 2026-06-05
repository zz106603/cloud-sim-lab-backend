package com.yunhwan.cloudsimlab.scenario;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog;
import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog.SeedDocument;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.scenario.adapter.out.persistence.ScenarioSeedCatalog;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioSeedPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FailureScenarioSeedTests {

	private static final List<String> FAILURE_SCENARIO_KEYS = List.of(
			"redis-failure-fallback",
			"rds-connection-pool-exhaustion",
			"alb-health-check-failure",
			"private-subnet-nat-missing",
			"security-group-misconfiguration"
	);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ScenarioSeedPort scenarioSeedPort;

	@Autowired
	private LearningDocumentSeedPort learningDocumentSeedPort;

	private Map<String, Scenario> scenariosByGraphKey;

	@BeforeEach
	void setUp() {
		LearningDocumentSeedCatalog.documents().forEach(this::saveDocument);
		scenariosByGraphKey = ScenarioSeedCatalog.scenarios().stream()
				.map(scenarioSeedPort::save)
				.collect(Collectors.toMap(Scenario::getGraphKey, Function.identity()));
	}

	@Test
	void 장애_시나리오_5개는_목록과_상세_API에서_조회된다() throws Exception {
		mockMvc.perform(get("/api/scenarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(10)))
				.andExpect(jsonPath("$[?(@.title == 'Redis 장애와 RDS fallback 부하 급증')]", hasSize(1)))
				.andExpect(jsonPath("$[?(@.title == 'RDS Connection Pool 고갈')]", hasSize(1)))
				.andExpect(jsonPath("$[?(@.title == 'ALB Health Check 실패')]", hasSize(1)))
				.andExpect(jsonPath("$[?(@.title == 'Private subnet NAT Gateway 또는 라우팅 누락')]", hasSize(1)))
				.andExpect(jsonPath("$[?(@.title == 'Security Group 오설정')]", hasSize(1)));

		for (String graphKey : FAILURE_SCENARIO_KEYS) {
			Scenario scenario = scenariosByGraphKey.get(graphKey);
			mockMvc.perform(get("/api/scenarios/{scenarioId}", scenario.getId()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(scenario.getId()))
					.andExpect(jsonPath("$.summary").value(scenario.getSummary()))
					.andExpect(jsonPath("$.description").value(scenario.getDescription()))
					.andExpect(jsonPath("$.initialArchitecture", hasSize(greaterThanOrEqualTo(4))))
					.andExpect(jsonPath("$.options", hasSize(3)))
					.andExpect(jsonPath("$.relatedLearningDocuments", hasSize(greaterThanOrEqualTo(1))));
		}
	}

	@Test
	void 장애_시나리오의_핵심_대응은_GOOD과_그래프_변화를_반환한다() throws Exception {
		assertCoreOptionGood("redis-failure-fallback", "rds-fallback-guard");
		assertCoreOptionGood("rds-connection-pool-exhaustion", "connection-pool");
		assertCoreOptionGood("alb-health-check-failure", "health-check");
		assertCoreOptionGood("private-subnet-nat-missing", "nat-gateway");
		assertCoreOptionGood("security-group-misconfiguration", "alb-security-group");
	}

	@Test
	void 장애_시나리오는_PARTIAL_RISKY_WRONG_선택을_구분한다() throws Exception {
		Scenario redisScenario = scenariosByGraphKey.get("redis-failure-fallback");
		Long partialOptionId = redisScenario.getOptions().get(1).getId();
		Long wrongOptionId = redisScenario.getOptions().get(2).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", redisScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(partialOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("PARTIAL"))
				.andExpect(jsonPath("$.detail", containsString("핵심 선택지가 빠져")));

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", redisScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(wrongOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("WRONG"))
				.andExpect(jsonPath("$.detail", containsString("원인을 직접 줄이는 선택지가 포함되지 않았습니다.")));

		Scenario securityScenario = scenariosByGraphKey.get("security-group-misconfiguration");
		Long riskyOptionId = securityScenario.getOptions().get(2).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", securityScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(riskyOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("RISKY"))
				.andExpect(jsonPath("$.riskScore").value(3))
				.andExpect(jsonPath("$.tradeOffSummary.security").value(-3));
	}

	private void assertCoreOptionGood(String graphKey, String expectedNodeId) throws Exception {
		Scenario scenario = scenariosByGraphKey.get(graphKey);
		Long coreOptionId = scenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", scenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(coreOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("GOOD"))
				.andExpect(jsonPath("$.selectedOptions[0].id").value(coreOptionId))
				.andExpect(jsonPath("$.relatedLearningDocuments", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.finalArchitectureGraph.nodes[?(@.id == '%s')]".formatted(expectedNodeId), hasSize(1)));
	}

	private void saveDocument(SeedDocument document) {
		learningDocumentSeedPort.save(LearningDocument.newDocumentWithKey(
				document.documentKey(),
				document.title(),
				document.category(),
				document.level(),
				document.summary(),
				document.contentFileName()
		));
	}
}
