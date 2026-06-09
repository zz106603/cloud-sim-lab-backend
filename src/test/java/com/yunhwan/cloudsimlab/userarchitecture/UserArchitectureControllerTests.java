package com.yunhwan.cloudsimlab.userarchitecture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserArchitectureControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void 빌더용_리소스와_연결_카탈로그를_조회할_수_있다() throws Exception {
		mockMvc.perform(get("/api/user-architectures/catalog"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resourceTypes", hasSize(17)))
				.andExpect(jsonPath("$.resourceTypes[0].key").value("CLIENT"))
				.andExpect(jsonPath("$.resourceTypes[0].displayName").value("Client"))
				.andExpect(jsonPath("$.resourceTypes[0].description").value("사용자 요청이 시작되는 외부 클라이언트입니다."))
				.andExpect(jsonPath("$.resourceTypes[0].visualizationCategory").value("ACTOR"))
				.andExpect(jsonPath("$.resourceTypes[0].learningPurpose").value("요청 흐름의 시작점과 외부 노출 경계를 표시할 때 사용합니다."))
				.andExpect(jsonPath("$.resourceTypes[3].key").value("EC2"))
				.andExpect(jsonPath("$.resourceTypes[5].key").value("TARGET_GROUP"))
				.andExpect(jsonPath("$.resourceTypes[11].key").value("CONNECTION_POOL"))
				.andExpect(jsonPath("$.connectionTypes", hasSize(5)))
				.andExpect(jsonPath("$.connectionTypes[0].key").value("REQUEST_FLOW"))
				.andExpect(jsonPath("$.connectionTypes[0].displayName").value("Request Flow"))
				.andExpect(jsonPath("$.connectionTypes[0].meaning").value("사용자 요청이나 애플리케이션 호출이 source에서 target으로 전달되는 흐름입니다."))
				.andExpect(jsonPath("$.connectionTypes[3].key").value("REPLICATION"));
	}

	@Test
	void 아키텍처를_생성하고_ID로_다시_조회할_수_있다() throws Exception {
		String architectureId = createArchitecture();

		mockMvc.perform(get("/api/user-architectures/{architectureId}", architectureId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.architectureId").value(architectureId))
				.andExpect(jsonPath("$.title").value("이벤트 조회 아키텍처"))
				.andExpect(jsonPath("$.description").value("조회 트래픽 분산을 연습합니다."))
				.andExpect(jsonPath("$.createdAt", notNullValue()))
				.andExpect(jsonPath("$.updatedAt", notNullValue()))
				.andExpect(jsonPath("$.nodes", hasSize(2)))
				.andExpect(jsonPath("$.nodes[0].id").value("ec2-1"))
				.andExpect(jsonPath("$.nodes[0].resourceType").value("EC2"))
				.andExpect(jsonPath("$.nodes[1].id").value("rds-1"))
				.andExpect(jsonPath("$.connections", hasSize(1)))
				.andExpect(jsonPath("$.connections[0].id").value("conn-1"))
				.andExpect(jsonPath("$.connections[0].sourceNodeId").value("ec2-1"))
				.andExpect(jsonPath("$.connections[0].targetNodeId").value("rds-1"));
	}

	@Test
	void 목록_응답은_그래프_상세를_포함하지_않고_요약만_반환한다() throws Exception {
		String architectureId = createArchitecture();

		mockMvc.perform(get("/api/user-architectures"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].architectureId").value(architectureId))
				.andExpect(jsonPath("$[0].title").value("이벤트 조회 아키텍처"))
				.andExpect(jsonPath("$[0].nodeCount").value(2))
				.andExpect(jsonPath("$[0].connectionCount").value(1))
				.andExpect(jsonPath("$[0].nodes").doesNotExist())
				.andExpect(jsonPath("$[0].connections").doesNotExist());
	}

	@Test
	void 저장된_아키텍처의_노드와_연결을_수정할_수_있다() throws Exception {
		String architectureId = createArchitecture();

		mockMvc.perform(put("/api/user-architectures/{architectureId}", architectureId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title":"ALB 추가 아키텍처",
								  "description":"트래픽 분산 진입점을 추가합니다.",
								  "nodes":[
								    {"id":"alb-1","resourceType":"ALB","displayName":"ALB"},
								    {"id":"ec2-1","resourceType":"EC2","displayName":"API 서버"}
								  ],
								  "connections":[
								    {"id":"conn-1","sourceNodeId":"alb-1","targetNodeId":"ec2-1","connectionType":"REQUEST_FLOW"}
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.architectureId").value(architectureId))
				.andExpect(jsonPath("$.title").value("ALB 추가 아키텍처"))
				.andExpect(jsonPath("$.nodes", hasSize(2)))
				.andExpect(jsonPath("$.nodes[0].id").value("alb-1"))
				.andExpect(jsonPath("$.nodes[1].id").value("ec2-1"))
				.andExpect(jsonPath("$.connections[0].id").value("conn-1"))
				.andExpect(jsonPath("$.connections[0].sourceNodeId").value("alb-1"))
				.andExpect(jsonPath("$.connections[0].targetNodeId").value("ec2-1"));
	}

	@Test
	void 아키텍처를_삭제하면_다시_조회할_수_없다() throws Exception {
		String architectureId = createArchitecture();

		mockMvc.perform(delete("/api/user-architectures/{architectureId}", architectureId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/user-architectures/{architectureId}", architectureId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("USER_ARCHITECTURE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("User architecture not found: " + architectureId));
	}

	@Test
	void 잘못된_연결_참조는_BAD_REQUEST_에러를_반환한다() throws Exception {
		mockMvc.perform(post("/api/user-architectures")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title":"잘못된 아키텍처",
								  "description":"",
								  "nodes":[{"id":"ec2-1","resourceType":"EC2","displayName":"API 서버"}],
								  "connections":[{"id":"conn-1","sourceNodeId":"ec2-1","targetNodeId":"rds-1","connectionType":"REQUEST_FLOW"}]
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_USER_ARCHITECTURE_REQUEST"))
				.andExpect(jsonPath("$.message").value("connection targetNodeId must reference an existing node: rds-1"));
	}

	@Test
	void 존재하지_않는_아키텍처_ID는_NOT_FOUND_에러를_반환한다() throws Exception {
		mockMvc.perform(get("/api/user-architectures/{architectureId}", "missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("USER_ARCHITECTURE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("User architecture not found: missing"));
	}

	private String createArchitecture() throws Exception {
		String location = mockMvc.perform(post("/api/user-architectures")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title":"이벤트 조회 아키텍처",
								  "description":"조회 트래픽 분산을 연습합니다.",
								  "nodes":[
								    {"id":"ec2-1","resourceType":"EC2","displayName":"API 서버"},
								    {"id":"rds-1","resourceType":"RDS","displayName":"주 데이터베이스"}
								  ],
								  "connections":[
								    {"id":"conn-1","sourceNodeId":"ec2-1","targetNodeId":"rds-1","connectionType":"REQUEST_FLOW"}
								  ]
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.LOCATION, notNullValue()))
				.andExpect(jsonPath("$.architectureId", notNullValue()))
				.andReturn()
				.getResponse()
				.getHeader(HttpHeaders.LOCATION);

		return location.substring(location.lastIndexOf('/') + 1);
	}
}
