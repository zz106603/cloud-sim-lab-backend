package com.yunhwan.cloudsimlab.learningmodule;

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
class LearningModuleControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 학습_모듈_목록은_경로와_orderIndex_순서로_반환된다() throws Exception {
		mockMvc.perform(get("/api/learning-modules"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(6)))
				.andExpect(jsonPath("$[0].id").value("single-server-deployment"))
				.andExpect(jsonPath("$[0].pathId").value("backend-aws-foundation"))
				.andExpect(jsonPath("$[0].orderIndex").value(1))
				.andExpect(jsonPath("$[5].id").value("user-architecture-practice"));
	}

	@Test
	void 학습_모듈_상세는_목표와_연결_ID를_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-modules/{moduleId}", "data-tier-scaling"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("data-tier-scaling"))
				.andExpect(jsonPath("$.learningGoals", hasSize(2)))
				.andExpect(jsonPath("$.documentIds", hasSize(4)))
				.andExpect(jsonPath("$.documentIds[0]").value("rds-connection-management"))
				.andExpect(jsonPath("$.relatedScenarioIds", hasSize(4)))
				.andExpect(jsonPath("$.relatedScenarioIds[0]").value("rds-failure"))
				.andExpect(jsonPath("$.practiceActivities", hasSize(9)))
				.andExpect(jsonPath("$.practiceActivities[0].type").value("READ_DOCUMENT"))
				.andExpect(jsonPath("$.practiceActivities[0].targetResourceId").value("rds-connection-management"))
				.andExpect(jsonPath("$.practiceActivities[0].recommendedOrder").value(1))
				.andExpect(jsonPath("$.practiceActivities[4].type").value("RUN_SCENARIO"))
				.andExpect(jsonPath("$.practiceActivities[4].targetResourceId").value("rds-failure"))
				.andExpect(jsonPath("$.practiceActivities[8].type").value("BUILD_ARCHITECTURE"))
				.andExpect(jsonPath("$.practiceActivities[8].targetResourceId").value("read-heavy-scaling-practice"));
	}

	@Test
	void 존재하지_않는_학습_모듈_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-modules/{moduleId}", "unknown-module"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("LEARNING_MODULE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Learning module not found: unknown-module"));
	}
}
