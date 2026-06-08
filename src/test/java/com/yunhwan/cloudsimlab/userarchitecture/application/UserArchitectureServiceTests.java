package com.yunhwan.cloudsimlab.userarchitecture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureCommandPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureQueryPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.ConnectionCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.CreateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.NodeCommand;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase.UpdateUserArchitectureCommand;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

class UserArchitectureServiceTests {

	private static final Instant NOW = Instant.parse("2026-06-08T00:00:00Z");

	private final InMemoryPort port = new InMemoryPort();
	private final UserArchitectureService service = new UserArchitectureService(
			port,
			port,
			Clock.fixed(NOW, ZoneOffset.UTC)
	);

	@Test
	void 생성_시_ID와_생성_수정_시각을_부여하고_노드와_연결_ID를_유지한다() {
		UserArchitecture created = service.create(new CreateUserArchitectureCommand(
				"조회 서비스 아키텍처",
				"읽기 트래픽 분산을 연습합니다.",
				List.of(
						new NodeCommand("ec2-1", UserArchitectureResourceType.EC2, "API 서버"),
						new NodeCommand("rds-1", UserArchitectureResourceType.RDS, "주 데이터베이스")
				),
				List.of(new ConnectionCommand("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		));

		assertThat(created.getArchitectureId()).isNotBlank();
		assertThat(created.getCreatedAt()).isEqualTo(NOW);
		assertThat(created.getUpdatedAt()).isEqualTo(NOW);
		assertThat(created.getNodes()).extracting(node -> node.id())
				.containsExactly("ec2-1", "rds-1");
		assertThat(created.getConnections()).extracting(connection -> connection.id())
				.containsExactly("conn-1");
	}

	@Test
	void 수정은_기존_생성_시각을_유지하고_그래프를_교체한다() {
		UserArchitecture created = service.create(new CreateUserArchitectureCommand(
				"초기 아키텍처",
				"",
				List.of(new NodeCommand("ec2-1", UserArchitectureResourceType.EC2, "API")),
				List.of()
		));

		UserArchitecture updated = service.update(created.getArchitectureId(), new UpdateUserArchitectureCommand(
				"수정된 아키텍처",
				"ALB를 추가합니다.",
				List.of(
						new NodeCommand("alb-1", UserArchitectureResourceType.ALB, "ALB"),
						new NodeCommand("ec2-1", UserArchitectureResourceType.EC2, "API")
				),
				List.of(new ConnectionCommand("conn-1", "alb-1", "ec2-1", UserArchitectureConnectionType.REQUEST_FLOW))
		));

		assertThat(updated.getArchitectureId()).isEqualTo(created.getArchitectureId());
		assertThat(updated.getTitle()).isEqualTo("수정된 아키텍처");
		assertThat(updated.getCreatedAt()).isEqualTo(created.getCreatedAt());
		assertThat(updated.getUpdatedAt()).isEqualTo(NOW);
		assertThat(updated.getNodes()).extracting(node -> node.id())
				.containsExactly("alb-1", "ec2-1");
		assertThat(updated.getConnections()).extracting(connection -> connection.sourceNodeId())
				.containsExactly("alb-1");
	}

	@Test
	void 존재하지_않는_아키텍처_수정과_삭제는_NOT_FOUND_예외를_던진다() {
		assertThatThrownBy(() -> service.update("missing", new UpdateUserArchitectureCommand("제목", "", List.of(), List.of())))
				.isInstanceOf(UserArchitectureNotFoundException.class)
				.hasMessage("User architecture not found: missing");

		assertThatThrownBy(() -> service.delete("missing"))
				.isInstanceOf(UserArchitectureNotFoundException.class)
				.hasMessage("User architecture not found: missing");
	}

	@Test
	void 잘못된_연결_참조는_요청_검증_예외로_변환한다() {
		assertThatThrownBy(() -> service.create(new CreateUserArchitectureCommand(
				"잘못된 아키텍처",
				"",
				List.of(new NodeCommand("ec2-1", UserArchitectureResourceType.EC2, "API")),
				List.of(new ConnectionCommand("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		)))
				.isInstanceOf(InvalidUserArchitectureRequestException.class)
				.hasMessage("connection targetNodeId must reference an existing node: rds-1");
	}

	private static class InMemoryPort implements UserArchitectureQueryPort, UserArchitectureCommandPort {

		private final Map<String, UserArchitecture> architectures = new LinkedHashMap<>();

		@Override
		public List<UserArchitecture> findAll() {
			return List.copyOf(architectures.values());
		}

		@Override
		public Optional<UserArchitecture> findById(String architectureId) {
			return Optional.ofNullable(architectures.get(architectureId));
		}

		@Override
		public UserArchitecture save(UserArchitecture architecture) {
			architectures.put(architecture.getArchitectureId(), architecture);
			return architecture;
		}

		@Override
		public boolean existsById(String architectureId) {
			return architectures.containsKey(architectureId);
		}

		@Override
		public void deleteById(String architectureId) {
			architectures.remove(architectureId);
		}
	}
}
