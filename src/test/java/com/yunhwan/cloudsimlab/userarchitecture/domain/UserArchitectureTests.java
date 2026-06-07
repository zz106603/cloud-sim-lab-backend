package com.yunhwan.cloudsimlab.userarchitecture.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class UserArchitectureTests {

	private static final Instant CREATED_AT = Instant.parse("2026-06-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-06-01T01:00:00Z");

	@Test
	void 사용자_작성_아키텍처는_기본_메타데이터와_리소스_연결을_표현한다() {
		UserArchitecture architecture = new UserArchitecture(
				"arch-1",
				"이벤트 조회 아키텍처",
				"조회 트래픽 분산을 연습합니다.",
				CREATED_AT,
				UPDATED_AT,
				List.of(
						new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API 서버"),
						new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "주 데이터베이스")
				),
				List.of(
						new UserArchitectureConnection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		);

		assertThat(architecture.getArchitectureId()).isEqualTo("arch-1");
		assertThat(architecture.getTitle()).isEqualTo("이벤트 조회 아키텍처");
		assertThat(architecture.getDescription()).isEqualTo("조회 트래픽 분산을 연습합니다.");
		assertThat(architecture.getCreatedAt()).isEqualTo(CREATED_AT);
		assertThat(architecture.getUpdatedAt()).isEqualTo(UPDATED_AT);
		assertThat(architecture.getNodes()).extracting(UserArchitectureNode::id)
				.containsExactly("ec2-1", "rds-1");
		assertThat(architecture.getConnections()).extracting(UserArchitectureConnection::sourceNodeId)
				.containsExactly("ec2-1");
	}

	@Test
	void 같은_타입의_리소스도_안정적인_개별_ID로_구분한다() {
		UserArchitecture architecture = new UserArchitecture(
				"arch-1",
				"이중화 아키텍처",
				null,
				CREATED_AT,
				UPDATED_AT,
				List.of(
						new UserArchitectureNode("ec2-a", UserArchitectureResourceType.EC2, "API 서버 A"),
						new UserArchitectureNode("ec2-b", UserArchitectureResourceType.EC2, "API 서버 B")
				),
				List.of()
		);

		assertThat(architecture.getDescription()).isEmpty();
		assertThat(architecture.getNodes()).extracting(UserArchitectureNode::id)
				.containsExactly("ec2-a", "ec2-b");
	}

	@Test
	void 노드와_연결은_입력_순서와_관계없이_ID_기준으로_보관한다() {
		UserArchitecture architecture = new UserArchitecture(
				"arch-1",
				"순서 독립 아키텍처",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(
						new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "DB"),
						new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API"),
						new UserArchitectureNode("alb-1", UserArchitectureResourceType.ALB, "ALB")
				),
				List.of(
						new UserArchitectureConnection("conn-2", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW),
						new UserArchitectureConnection("conn-1", "alb-1", "ec2-1", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		);

		assertThat(architecture.getNodes()).extracting(UserArchitectureNode::id)
				.containsExactly("alb-1", "ec2-1", "rds-1");
		assertThat(architecture.getConnections()).extracting(UserArchitectureConnection::id)
				.containsExactly("conn-1", "conn-2");
	}

	@Test
	void 같은_구성은_입력_순서가_달라도_같은_아키텍처로_비교된다() {
		UserArchitecture first = new UserArchitecture(
				"arch-1",
				"순서 독립 아키텍처",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(
						new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "DB"),
						new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API")
				),
				List.of(
						new UserArchitectureConnection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		);
		UserArchitecture second = new UserArchitecture(
				"arch-1",
				"순서 독립 아키텍처",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(
						new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API"),
						new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "DB")
				),
				List.of(
						new UserArchitectureConnection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		);

		assertThat(first).isEqualTo(second);
		assertThat(first).hasSameHashCodeAs(second);
	}

	@Test
	void 컬렉션_입력은_복사해서_외부_변경을_막는다() {
		List<UserArchitectureNode> nodes = new ArrayList<>();
		nodes.add(new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API"));

		UserArchitecture architecture = new UserArchitecture(
				"arch-1",
				"복사 테스트",
				"",
				CREATED_AT,
				UPDATED_AT,
				nodes,
				List.of()
		);
		nodes.add(new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "DB"));

		assertThat(architecture.getNodes()).extracting(UserArchitectureNode::id)
				.containsExactly("ec2-1");
		assertThatThrownBy(() -> architecture.getNodes().add(new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "DB")))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void 기본_도메인_값이_잘못되면_예외가_발생한다() {
		assertThatThrownBy(() -> new UserArchitectureNode(" ", UserArchitectureResourceType.EC2, "API"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("node id must not be blank");
		assertThatThrownBy(() -> new UserArchitectureNode("ec2-1", null, "API"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("node resourceType must not be null");
		assertThatThrownBy(() -> new UserArchitectureConnection("conn-1", "ec2-1", " ", UserArchitectureConnectionType.REQUEST_FLOW))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("connection targetNodeId must not be blank");
		assertThatThrownBy(() -> new UserArchitecture(" ", "제목", "", CREATED_AT, UPDATED_AT, List.of(), List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("architectureId must not be blank");
		assertThatThrownBy(() -> new UserArchitecture("arch-1", "제목", "", UPDATED_AT, CREATED_AT, List.of(), List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("updatedAt must not be before createdAt");
	}

	@Test
	void 중복_ID와_존재하지_않는_연결_참조를_거부한다() {
		UserArchitectureNode api = new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API");
		UserArchitectureNode duplicateApi = new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API 복제");

		assertThatThrownBy(() -> new UserArchitecture(
				"arch-1",
				"중복 노드",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(api, duplicateApi),
				List.of()
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("node id must be unique: ec2-1");

		assertThatThrownBy(() -> new UserArchitecture(
				"arch-1",
				"누락된 대상",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(api),
				List.of(new UserArchitectureConnection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("connection targetNodeId must reference an existing node: rds-1");

		assertThatThrownBy(() -> new UserArchitecture(
				"arch-1",
				"중복 연결",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(api),
				List.of(
						new UserArchitectureConnection("conn-1", "ec2-1", "ec2-1", UserArchitectureConnectionType.DEPENDS_ON),
						new UserArchitectureConnection("conn-1", "ec2-1", "ec2-1", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("connection id must be unique: conn-1");
	}

	@Test
	void 노드와_연결_목록의_null_요소를_의도한_도메인_예외로_거부한다() {
		List<UserArchitectureNode> nodes = new ArrayList<>();
		nodes.add(null);
		assertThatThrownBy(() -> new UserArchitecture(
				"arch-1",
				"null 노드",
				"",
				CREATED_AT,
				UPDATED_AT,
				nodes,
				List.of()
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("nodes must not contain null");

		List<UserArchitectureConnection> connections = new ArrayList<>();
		connections.add(null);
		assertThatThrownBy(() -> new UserArchitecture(
				"arch-1",
				"null 연결",
				"",
				CREATED_AT,
				UPDATED_AT,
				List.of(new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API")),
				connections
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("connections must not contain null");
	}
}
