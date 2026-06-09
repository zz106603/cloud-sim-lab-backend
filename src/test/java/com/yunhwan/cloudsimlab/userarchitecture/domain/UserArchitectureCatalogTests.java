package com.yunhwan.cloudsimlab.userarchitecture.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserArchitectureCatalogTests {

	@Test
	void 리소스_카탈로그는_중복되지_않는_안정적인_키와_필수_설명을_가진다() {
		assertThat(UserArchitectureResourceType.values())
				.extracting(UserArchitectureResourceType::getKey)
				.doesNotHaveDuplicates();
		assertThat(UserArchitectureResourceType.values())
				.allSatisfy(resourceType -> {
					assertThat(resourceType.getKey()).isNotBlank();
					assertThat(resourceType.getDisplayName()).isNotBlank();
					assertThat(resourceType.getDescription()).isNotBlank();
					assertThat(resourceType.getVisualizationCategory()).isNotBlank();
					assertThat(resourceType.getLearningPurpose()).isNotBlank();
				});
	}

	@Test
	void 연결_카탈로그는_중복되지_않는_안정적인_키와_필수_설명을_가진다() {
		assertThat(UserArchitectureConnectionType.values())
				.extracting(UserArchitectureConnectionType::getKey)
				.doesNotHaveDuplicates();
		assertThat(UserArchitectureConnectionType.values())
				.allSatisfy(connectionType -> {
					assertThat(connectionType.getKey()).isNotBlank();
					assertThat(connectionType.getDisplayName()).isNotBlank();
					assertThat(connectionType.getMeaning()).isNotBlank();
				});
	}

	@Test
	void 카탈로그는_현재_시나리오와_학습_문서의_핵심_컴포넌트를_포함한다() {
		assertThat(Arrays.asList(UserArchitectureResourceType.values()))
				.contains(
						UserArchitectureResourceType.CLIENT,
						UserArchitectureResourceType.EC2,
						UserArchitectureResourceType.ALB,
						UserArchitectureResourceType.TARGET_GROUP,
						UserArchitectureResourceType.AUTO_SCALING_GROUP,
						UserArchitectureResourceType.RDS,
						UserArchitectureResourceType.RDS_STANDBY,
						UserArchitectureResourceType.READ_REPLICA,
						UserArchitectureResourceType.REDIS,
						UserArchitectureResourceType.CONNECTION_POOL,
						UserArchitectureResourceType.HEALTH_CHECK,
						UserArchitectureResourceType.SUBNET,
						UserArchitectureResourceType.NAT_GATEWAY,
						UserArchitectureResourceType.INTERNET_GATEWAY,
						UserArchitectureResourceType.SECURITY_GROUP
				);
		assertThat(Arrays.asList(UserArchitectureConnectionType.values()))
				.contains(
						UserArchitectureConnectionType.REQUEST_FLOW,
						UserArchitectureConnectionType.NETWORK_ROUTE,
						UserArchitectureConnectionType.DEPENDS_ON,
						UserArchitectureConnectionType.REPLICATION,
						UserArchitectureConnectionType.SECURITY_RULE
				);
	}
}
