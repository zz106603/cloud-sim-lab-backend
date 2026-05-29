package com.yunhwan.cloudsimlab.scenario;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
	private Scenario networkScenario;
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
				"웹 서비스 확장",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"컴퓨팅 용량을 선택합니다.",
				"트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다.",
				List.of("Client", "EC2", "RDS"),
				List.of(
						ScenarioOption.newOption("작은 EC2 인스턴스 유지", "비용은 낮지만 용량이 제한적입니다.", 1, false, 0),
						ScenarioOption.newOption("큰 EC2 인스턴스로 변경", "용량은 늘어나지만 비용도 증가합니다.", 2, true, 0)
				)
		));
		networkScenario = seedPort.save(Scenario.newScenario(
				"퍼블릭/프라이빗 트래픽 분리",
				ScenarioCategory.NETWORK,
				ScenarioLevel.INTERMEDIATE,
				"기본 네트워크 경계를 설계합니다.",
				"필요한 네트워크 표면만 외부에 노출해야 합니다.",
				List.of("Client", "Public subnet", "Application server"),
				List.of(
						ScenarioOption.newOption("Public subnet에 배치", "리소스가 인터넷 인바운드 트래픽에 노출됩니다.", 1, false, 2)
				)
		));
	}

	@Test
	void 시나리오_목록은_상세_이동에_사용할_ID와_요약_정보를_반환한다() throws Exception {
		mockMvc.perform(get("/api/scenarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", notNullValue()))
				.andExpect(jsonPath("$[0].id").value(computeScenario.getId()))
				.andExpect(jsonPath("$[0].title").value("웹 서비스 확장"))
				.andExpect(jsonPath("$[0].category").value("COMPUTE"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].summary").value("컴퓨팅 용량을 선택합니다."))
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
				.andExpect(jsonPath("$.title").value("웹 서비스 확장"))
				.andExpect(jsonPath("$.description").value("트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다."))
				.andExpect(jsonPath("$.problem").value("트래픽이 늘어나기 전에 EC2 용량을 비교해야 합니다."))
				.andExpect(jsonPath("$.initialArchitecture", hasSize(3)))
				.andExpect(jsonPath("$.initialArchitecture[0]").value("Client"))
				.andExpect(jsonPath("$.initialArchitectureGraph.nodes", hasSize(3)))
				.andExpect(jsonPath("$.initialArchitectureGraph.nodes[0].id").value("client"))
				.andExpect(jsonPath("$.initialArchitectureGraph.nodes[1].type").value("EC2"))
				.andExpect(jsonPath("$.initialArchitectureGraph.edges", hasSize(2)))
				.andExpect(jsonPath("$.initialArchitectureGraph.edges[0].source").value("client"))
				.andExpect(jsonPath("$.initialArchitectureGraph.edges[0].target").value("ec2"))
				.andExpect(jsonPath("$.options", hasSize(2)))
				.andExpect(jsonPath("$.options[0].name").value("작은 EC2 인스턴스 유지"))
				.andExpect(jsonPath("$.options[0].description").value("비용은 낮지만 용량이 제한적입니다."));
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
				.andExpect(jsonPath("$.summary").value("선택한 구성이 시나리오 요구를 잘 해결합니다."))
				.andExpect(jsonPath("$.detail", containsString("시나리오 목표인 '컴퓨팅 용량을 선택합니다.'")))
				.andExpect(jsonPath("$.detail", containsString("큰 EC2 인스턴스로 변경은 용량은 늘어나지만 비용도 증가합니다.")))
				.andExpect(jsonPath("$.detail", containsString("핵심 선택지는")))
				.andExpect(jsonPath("$.selectedOptions", hasSize(1)))
				.andExpect(jsonPath("$.selectedOptions[0].id").value(coreOptionId))
				.andExpect(jsonPath("$.finalArchitecture", hasSize(3)))
				.andExpect(jsonPath("$.finalArchitecture[1]").value("EC2"))
				.andExpect(jsonPath("$.relatedLearningDocuments", hasSize(1)))
				.andExpect(jsonPath("$.relatedLearningDocuments[0].id").value(computeDocument.getId()))
				.andExpect(jsonPath("$.relatedLearningDocuments[0].title").value("Virtual machines and compute capacity"));
	}

	@Test
	void CORS_허용_origin은_설정값으로_동작한다() throws Exception {
		mockMvc.perform(options("/api/scenarios")
						.header(HttpHeaders.ORIGIN, "http://localhost:5173")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
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
				.andExpect(jsonPath("$.resultType").value("GOOD"))
				.andExpect(jsonPath("$.detail", containsString("유효한 선택지가 시나리오 목표에 맞게 현재 문제를 줄입니다.")));
	}

	@Test
	void 핵심_선택지가_빠진_유효한_선택지는_PARTIAL_결과와_한계를_반환한다() throws Exception {
		Long partialOptionId = computeScenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", computeScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(partialOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("PARTIAL"))
				.andExpect(jsonPath("$.score").value(1))
				.andExpect(jsonPath("$.riskScore").value(0))
				.andExpect(jsonPath("$.detail", containsString("핵심 선택지가 빠져 주요 병목이나 장애 지점이 남습니다.")))
				.andExpect(jsonPath("$.detail", containsString("작은 EC2 인스턴스 유지는 비용은 낮지만 용량이 제한적입니다.")));
	}

	@Test
	void 위험_점수가_높은_선택지는_RISKY_결과와_위험_설명을_반환한다() throws Exception {
		Long riskyOptionId = networkScenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", networkScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(riskyOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("RISKY"))
				.andExpect(jsonPath("$.score").value(1))
				.andExpect(jsonPath("$.riskScore").value(2))
				.andExpect(jsonPath("$.detail", containsString("위험 점수가 높아 부작용을 먼저 검토해야 합니다.")))
				.andExpect(jsonPath("$.detail", containsString("보안 노출")));
	}

	@Test
	void 문제_원인과_맞지_않는_선택지는_WRONG_결과와_재판단_안내를_반환한다() throws Exception {
		Scenario wrongScenario = seedPort.save(Scenario.newScenario(
				"DB 병목 대응",
				ScenarioCategory.STORAGE,
				ScenarioLevel.INTERMEDIATE,
				"RDS 조회 병목을 줄입니다.",
				"조회 요청 증가로 RDS CPU가 상승했습니다.",
				List.of("Client", "EC2", "RDS"),
				List.of(
						ScenarioOption.newOption("ALB만 추가", "진입점은 정리되지만 RDS 조회 병목은 줄이지 못합니다.", 0, false, 0)
				)
		));
		Long wrongOptionId = wrongScenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", wrongScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(wrongOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("WRONG"))
				.andExpect(jsonPath("$.score").value(0))
				.andExpect(jsonPath("$.riskScore").value(0))
				.andExpect(jsonPath("$.detail", containsString("원인을 직접 줄이는 선택지가 포함되지 않았습니다.")))
				.andExpect(jsonPath("$.detail", containsString("점수가 없는 선택지")));
	}

	@Test
	void 여러_핵심_선택지가_대안일_때_Redis_단독_선택도_GOOD_결과를_반환한다() throws Exception {
		Scenario readHeavyScenario = seedPort.save(readHeavyScenario());
		Long redisOptionId = readHeavyScenario.getOptions().get(0).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", readHeavyScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(redisOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("GOOD"))
				.andExpect(jsonPath("$.detail", containsString("Redis Cache 추가는 반복 조회를 빠르게 처리하고 RDS 부하를 줄입니다.")))
				.andExpect(jsonPath("$.finalArchitectureGraph.nodes[?(@.id == 'redis')]", hasSize(1)))
				.andExpect(jsonPath("$.finalArchitectureGraph.nodes[?(@.type == 'REDIS')]", hasSize(1)))
				.andExpect(jsonPath("$.finalArchitectureGraph.edges[?(@.source == 'ec2' && @.target == 'redis')]", hasSize(1)));
	}

	@Test
	void 여러_핵심_선택지가_대안일_때_ReadReplica_단독_선택도_GOOD_결과를_반환한다() throws Exception {
		Scenario readHeavyScenario = seedPort.save(readHeavyScenario());
		Long readReplicaOptionId = readHeavyScenario.getOptions().get(1).getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", readHeavyScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(readReplicaOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultType").value("GOOD"))
				.andExpect(jsonPath("$.detail", containsString("Read Replica 추가는 읽기 부하를 분산합니다.")));
	}

	@Test
	void 영문으로_끝나는_선택지명은_자연스러운_조사로_피드백을_반환한다() throws Exception {
		Scenario redisScenario = seedPort.save(Scenario.newScenario(
				"Redis 캐시 적용",
				ScenarioCategory.STORAGE,
				ScenarioLevel.BEGINNER,
				"반복 조회 응답 시간을 줄입니다.",
				"반복 조회가 많아 캐시 도입을 검토합니다.",
				List.of("Client", "EC2", "RDS"),
				List.of(
						ScenarioOption.newOption("Redis", "반복 조회를 캐시로 처리합니다.", 2, true, 0)
				)
		));
		Long redisOptionId = redisScenario.getOptions().getFirst().getId();

		mockMvc.perform(post("/api/scenarios/{scenarioId}/simulate", redisScenario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"selectedOptionIds":[%d]}
								""".formatted(redisOptionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.detail", containsString("Redis는 반복 조회를 캐시로 처리합니다.")));
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

	private Scenario readHeavyScenario() {
		return Scenario.newScenario(
				"조회 중심 성능 문제",
				ScenarioCategory.STORAGE,
				ScenarioLevel.INTERMEDIATE,
				"읽기 트래픽이 많은 API의 RDS 부하를 낮춥니다.",
				"상품 목록과 상세 조회가 급증하면서 RDS CPU와 쿼리 시간이 상승했습니다.",
				List.of("Client", "ALB", "EC2", "RDS"),
				List.of(
						ScenarioOption.newOption("Redis Cache 추가", "반복 조회를 빠르게 처리하고 RDS 부하를 줄입니다.", 2, true, 0),
						ScenarioOption.newOption("Read Replica 추가", "읽기 부하를 분산합니다.", 2, true, 0),
						ScenarioOption.newOption("EC2만 증설", "RDS 조회 병목은 그대로 남을 수 있습니다.", 1, false, 1)
				)
		);
	}
}
