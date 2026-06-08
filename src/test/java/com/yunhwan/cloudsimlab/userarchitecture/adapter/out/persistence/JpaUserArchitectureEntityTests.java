package com.yunhwan.cloudsimlab.userarchitecture.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureNode;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

class JpaUserArchitectureEntityTests {

	@Test
	void fromPreservesArchitectureNodeAndConnectionIds() {
		Instant createdAt = Instant.parse("2026-06-01T00:00:00Z");
		Instant updatedAt = Instant.parse("2026-06-01T01:00:00Z");
		UserArchitecture architecture = new UserArchitecture(
				"arch-1",
				"이벤트 조회 아키텍처",
				"조회 트래픽 분산을 연습합니다.",
				createdAt,
				updatedAt,
				List.of(
						new UserArchitectureNode("ec2-1", UserArchitectureResourceType.EC2, "API 서버"),
						new UserArchitectureNode("rds-1", UserArchitectureResourceType.RDS, "주 데이터베이스")
				),
				List.of(new UserArchitectureConnection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		);

		UserArchitecture mapped = JpaUserArchitectureEntity.from(architecture).toDomain();

		assertThat(mapped.getArchitectureId()).isEqualTo("arch-1");
		assertThat(mapped.getCreatedAt()).isEqualTo(createdAt);
		assertThat(mapped.getUpdatedAt()).isEqualTo(updatedAt);
		assertThat(mapped.getNodes()).extracting(UserArchitectureNode::id)
				.containsExactly("ec2-1", "rds-1");
		assertThat(mapped.getConnections()).extracting(UserArchitectureConnection::id)
				.containsExactly("conn-1");
		assertThat(mapped.getConnections().getFirst().sourceNodeId()).isEqualTo("ec2-1");
		assertThat(mapped.getConnections().getFirst().targetNodeId()).isEqualTo("rds-1");
	}
}
