package com.yunhwan.cloudsimlab.architecturepractice;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ArchitecturePracticeControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 아키텍처_연습_템플릿_목록을_조회할_수_있다() throws Exception {
		mockMvc.perform(get("/api/architecture-practices"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].id").value("alb-private-subnet-application"))
				.andExpect(jsonPath("$[0].title").value("ALB + private subnet 애플리케이션"))
				.andExpect(jsonPath("$[0].level").value("BEGINNER"))
				.andExpect(jsonPath("$[0].requiredResourceTypes", hasItems("ALB", "EC2", "RDS")))
				.andExpect(jsonPath("$[0].relatedScenarioIds", hasItems("private-subnet-app")));
	}

	@Test
	void 아키텍처_연습_템플릿_상세는_starter_그래프와_연결_ID를_반환한다() throws Exception {
		mockMvc.perform(get("/api/architecture-practices/{practiceId}", "read-heavy-scaling-practice"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("read-heavy-scaling-practice"))
				.andExpect(jsonPath("$.instructions", hasSize(3)))
				.andExpect(jsonPath("$.starterNodes", hasSize(6)))
				.andExpect(jsonPath("$.starterNodes[3].resourceType").value("REDIS"))
				.andExpect(jsonPath("$.starterConnections", hasSize(7)))
				.andExpect(jsonPath("$.starterConnections[*].connectionType", hasItems("REQUEST_FLOW", "REPLICATION")))
				.andExpect(jsonPath("$.requiredResourceTypes", hasItems("READ_REPLICA", "REDIS")))
				.andExpect(jsonPath("$.relatedDocumentIds", hasItems("read-replica-read-scaling", "redis-cache")))
				.andExpect(jsonPath("$.relatedScenarioIds", hasItems("read-heavy-performance")))
				.andExpect(jsonPath("$.relatedModuleIds", hasItems("data-tier-scaling")));
	}

	@Test
	void 존재하지_않는_아키텍처_연습_템플릿_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/architecture-practices/{practiceId}", "missing-practice"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ARCHITECTURE_PRACTICE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Architecture practice not found: missing-practice"));
	}
}
