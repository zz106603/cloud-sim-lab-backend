package com.yunhwan.cloudsimlab.learningpath;

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
class LearningPathControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 학습_경로_목록은_orderIndex_순서와_추천_여부를_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-paths"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value("backend-aws-foundation"))
				.andExpect(jsonPath("$[0].targetLevel").value("BEGINNER"))
				.andExpect(jsonPath("$[0].recommended").value(true))
				.andExpect(jsonPath("$[0].orderIndex").value(1))
				.andExpect(jsonPath("$[0].moduleIds", hasSize(6)))
				.andExpect(jsonPath("$[0].moduleIds[0]").value("single-server-deployment"))
				.andExpect(jsonPath("$[0].moduleIds[5]").value("user-architecture-practice"));
	}

	@Test
	void 학습_경로_상세는_모듈별_문서_시나리오_아키텍처_연습_연결을_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-paths/{pathId}", "backend-aws-foundation"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("backend-aws-foundation"))
				.andExpect(jsonPath("$.modules", hasSize(6)))
				.andExpect(jsonPath("$.modules[0].id").value("single-server-deployment"))
				.andExpect(jsonPath("$.modules[0].learningGoals", hasSize(2)))
				.andExpect(jsonPath("$.modules[0].documentIds[0]").value("ec2-compute-capacity"))
				.andExpect(jsonPath("$.modules[0].relatedScenarioIds[0]").value("single-spring-boot"))
				.andExpect(jsonPath("$.modules[5].relatedArchitecturePracticeIds[0]").value("architecture-builder-basic"));
	}

	@Test
	void 존재하지_않는_학습_경로_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/learning-paths/{pathId}", "unknown-path"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("LEARNING_PATH_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Learning path not found: unknown-path"));
	}
}
