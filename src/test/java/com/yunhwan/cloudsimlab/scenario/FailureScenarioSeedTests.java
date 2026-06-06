package com.yunhwan.cloudsimlab.scenario;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraphs;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

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
	void 장애_시나리오_상세는_초기_장애_영향_흐름을_반환한다() throws Exception {
		Scenario rdsFailureScenario = scenariosByGraphKey.get("rds-failure");

		mockMvc.perform(get("/api/scenarios/{scenarioId}", rdsFailureScenario.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.initialFailureImpact.failureSourceNodeId").value("rds"))
				.andExpect(jsonPath("$.initialFailureImpact.affectedNodeIds[?(@ == 'rds')]", hasSize(1)))
				.andExpect(jsonPath("$.initialFailureImpact.affectedEdges[?(@.source == 'ec2' && @.target == 'rds')]", hasSize(1)))
				.andExpect(jsonPath("$.initialFailureImpact.userSymptoms[0]", containsString("쓰기와 조회 요청")))
				.andExpect(jsonPath("$.initialFailureImpact.remainingRisks[0]", containsString("단일 AZ RDS 장애")));
	}

	@Test
	void 시뮬레이션은_복구된_경로와_남은_장애_영향을_구분한다() throws Exception {
		Scenario redisScenario = scenariosByGraphKey.get("redis-failure-fallback");
		Long coreOptionId = optionId(redisScenario, ScenarioOption::isCore);

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", redisScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(coreOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.failureImpactResult.recoveredEdges[?(@.source == 'ec2' && @.target == 'rds-fallback-guard')]", hasSize(1)))
				.andExpect(jsonPath("$.failureImpactResult.recoveredEdges[?(@.source == 'rds-fallback-guard' && @.target == 'rds')]", hasSize(1)))
				.andExpect(jsonPath("$.failureImpactResult.remainingImpact.failureSourceNodeId").value("redis"))
				.andExpect(jsonPath("$.failureImpactResult.remainingImpact.affectedNodeIds", hasSize(0)))
				.andExpect(jsonPath("$.failureImpactResult.remainingImpact.remainingRisks[0]", containsString("Redis 자체 복구")))
				.andExpect(jsonPath("$.failureImpactResult.postActionNotes[0]", containsString("RDS 포화")));
	}

	@Test
	void 핵심_대응이_빠진_시뮬레이션은_초기_장애_영향을_잔여_영향으로_남긴다() throws Exception {
		Scenario albScenario = scenariosByGraphKey.get("alb-health-check-failure");
		Long partialOptionId = optionId(albScenario, option -> option.getScore() > 0 && !option.isCore());

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", albScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(partialOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("PARTIAL"))
				.andExpect(jsonPath("$.failureImpactResult.recoveredEdges", hasSize(0)))
				.andExpect(jsonPath("$.failureImpactResult.remainingImpact.failureSourceNodeId").value("target-group"))
				.andExpect(jsonPath("$.failureImpactResult.remainingImpact.affectedEdges[?(@.source == 'target-group' && @.target == 'ec2')]", hasSize(1)))
				.andExpect(jsonPath("$.failureImpactResult.postActionNotes[0]", containsString("직접 복구하지 못해")));
	}

	@Test
	void 장애_시나리오_핵심_대응_그래프는_중복되거나_오해되는_요소를_남기지_않는다() {
		for (String graphKey : FAILURE_SCENARIO_KEYS) {
			Scenario scenario = scenariosByGraphKey.get(graphKey);
			ScenarioOption coreOption = option(scenario, ScenarioOption::isCore);

			List<String> edgeKeys = ArchitectureGraphs.finalFor(scenario, List.of(coreOption)).edges().stream()
					.map(edge -> edge.source() + "->" + edge.target())
					.toList();

			assertThat(edgeKeys)
					.as(graphKey + " final graph source-target edges")
					.doesNotHaveDuplicates();
		}

		Scenario securityScenario = scenariosByGraphKey.get("security-group-misconfiguration");
		ScenarioOption coreOption = option(securityScenario, ScenarioOption::isCore);

		assertThat(ArchitectureGraphs.finalFor(securityScenario, List.of(coreOption)).nodes())
				.extracting(node -> node.id())
				.doesNotContain("security-group")
				.contains("alb-security-group", "ec2-security-group", "rds-security-group");
	}

	@Test
	void 장애_시나리오는_PARTIAL_RISKY_WRONG_선택을_구분한다() throws Exception {
		Scenario redisScenario = scenariosByGraphKey.get("redis-failure-fallback");
		Long partialOptionId = optionId(redisScenario, option -> option.getScore() > 0 && !option.isCore() && option.getRiskScore() == 1);
		Long wrongOptionId = optionId(redisScenario, option -> option.getScore() == 0);

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
		Long riskyOptionId = optionId(securityScenario, option -> option.getRiskScore() == 3);

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
		Long coreOptionId = optionId(scenario, ScenarioOption::isCore);

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

	private Long optionId(Scenario scenario, java.util.function.Predicate<ScenarioOption> predicate) {
		return option(scenario, predicate).getId();
	}

	private ScenarioOption option(Scenario scenario, java.util.function.Predicate<ScenarioOption> predicate) {
		return scenario.getOptions().stream()
				.filter(predicate)
				.findFirst()
				.orElseThrow();
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
