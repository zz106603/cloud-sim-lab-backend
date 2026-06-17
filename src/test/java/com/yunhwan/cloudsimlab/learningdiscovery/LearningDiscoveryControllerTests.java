package com.yunhwan.cloudsimlab.learningdiscovery;

import static org.hamcrest.Matchers.hasItems;
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
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LearningDiscoveryControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LearningDocumentSeedPort learningDocumentSeedPort;

	@Autowired
	private ScenarioSeedPort scenarioSeedPort;

	@BeforeEach
	void setUp() {
		learningDocumentSeedPort.save(LearningDocument.newDocumentWithMetadata(
				"ec2-compute-capacity",
				"EC2와 컴퓨팅 용량",
				DocumentCategory.EC2,
				DocumentLevel.BEGINNER,
				"EC2 용량 선택 기준을 확인합니다.",
				"EC2 본문",
				1,
				List.of(),
				List.of("EC2", "compute", "capacity"),
				List.of("single-server-deployment"),
				List.of("single-spring-boot")
		));
		learningDocumentSeedPort.save(LearningDocument.newDocumentWithMetadata(
				"redis-cache",
				"Redis Cache",
				DocumentCategory.REDIS,
				DocumentLevel.INTERMEDIATE,
				"캐시 적용 기준을 확인합니다.",
				"Redis 본문",
				10,
				List.of("rds-connection-management"),
				List.of("Redis", "cache", "consistency"),
				List.of("data-tier-scaling"),
				List.of("read-heavy-performance")
		));
		scenarioSeedPort.save(Scenario.newScenarioWithLearningContext(
				"single-spring-boot",
				"단일 Spring Boot 배포",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"단일 서버 확장 한계를 확인합니다.",
				"단일 EC2 구조를 비교합니다.",
				List.of("Client", "EC2"),
				List.of("single-server-deployment"),
				List.of(new ScenarioPrerequisiteConcept(
						"ec2-capacity",
						"EC2 용량",
						"ec2-compute-capacity",
						"EC2 용량 판단이 필요합니다."
				)),
				new ScenarioObservationPoint(
						"CPU",
						"단일 EC2",
						"Client -> EC2",
						"외부 노출",
						"변화 없음",
						"성능과 비용"
				),
				List.of("performance", "availability"),
				List.of(ScenarioOption.newOption("큰 EC2 인스턴스", "용량을 늘립니다.", 2, true, 0))
		));
		scenarioSeedPort.save(Scenario.newScenarioWithLearningContext(
				"read-heavy-performance",
				"조회 부하 성능 개선",
				ScenarioCategory.STORAGE,
				ScenarioLevel.INTERMEDIATE,
				"읽기 부하 분산을 비교합니다.",
				"Redis와 Read Replica를 비교합니다.",
				List.of("Client", "EC2", "RDS"),
				List.of("data-tier-scaling"),
				List.of(new ScenarioPrerequisiteConcept(
						"redis-cache",
						"Redis Cache",
						"redis-cache",
						"캐시 정합성 판단이 필요합니다."
				)),
				null,
				List.of("performance", "consistency"),
				List.of(ScenarioOption.newOption("Redis 추가", "반복 조회를 캐시합니다.", 2, true, 0))
		));
	}

	@Test
	void 학습_관계_탐색은_문서_시나리오_모듈_아키텍처_연습을_결정적_순서로_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-discovery"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(13)))
				.andExpect(jsonPath("$[0].resourceType").value("ARCHITECTURE_PRACTICE"))
				.andExpect(jsonPath("$[0].id").value("architecture-builder-basic"))
				.andExpect(jsonPath("$[0].recommendedPathIncluded").value(true))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].relatedDocumentIds", hasItems("ec2-compute-capacity")))
				.andExpect(jsonPath("$[1].resourceType").value("DOCUMENT"))
				.andExpect(jsonPath("$[1].id").value("ec2-compute-capacity"))
				.andExpect(jsonPath("$[1].relatedScenarioIds", hasItems("single-spring-boot")))
				.andExpect(jsonPath("$[1].relatedArchitecturePracticeIds", hasItems("architecture-builder-basic")));
	}

	@Test
	void 학습_관계_탐색은_리소스_타입과_태그로_필터링된다() throws Exception {
		mockMvc.perform(get("/api/learning-discovery")
						.param("resourceType", "document")
						.param("tag", "redis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].resourceType").value("DOCUMENT"))
				.andExpect(jsonPath("$[0].id").value("redis-cache"))
				.andExpect(jsonPath("$[0].conceptTags", hasItems("Redis", "cache")));
	}

	@Test
	void 학습_관계_탐색은_카테고리와_난이도로_필터링된다() throws Exception {
		mockMvc.perform(get("/api/learning-discovery")
						.param("category", "STORAGE")
						.param("level", "INTERMEDIATE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].resourceType").value("SCENARIO"))
				.andExpect(jsonPath("$[0].id").value("read-heavy-performance"));
	}

	@Test
	void 학습_관계_탐색은_존재하지_않는_필터에_빈_배열을_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-discovery")
						.param("category", "UNKNOWN")
						.param("resourceType", "DOCUMENT"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}
}
