package com.yunhwan.cloudsimlab.userarchitecture.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

class UserArchitectureComparatorTests {

	private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");

	@Test
	void 사용자_아키텍처는_입력_순서가_달라도_같은_ID와_의미를_유지로_비교한다() {
		UserArchitecture base = architecture(
				"base",
				"기준",
				List.of(
						node("rds-1", UserArchitectureResourceType.RDS, "DB"),
						node("ec2-1", UserArchitectureResourceType.EC2, "API")
				),
				List.of(connection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		);
		UserArchitecture target = architecture(
				"target",
				"대상",
				List.of(
						node("ec2-1", UserArchitectureResourceType.EC2, "API"),
						node("rds-1", UserArchitectureResourceType.RDS, "DB")
				),
				List.of(connection("conn-1", "ec2-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		);

		UserArchitectureComparisonResult result = UserArchitectureComparator.compare(base, target);

		assertThat(result.resources().unchanged()).hasSize(2);
		assertThat(result.resources().added()).isEmpty();
		assertThat(result.resources().removed()).isEmpty();
		assertThat(result.resources().changed()).isEmpty();
		assertThat(result.connections().unchanged()).hasSize(1);
	}

	@Test
	void 같은_ID의_리소스와_연결_의미가_바뀌면_CHANGED로_비교한다() {
		UserArchitecture base = architecture(
				"base",
				"기준",
				List.of(
						node("app-1", UserArchitectureResourceType.EC2, "API"),
						node("rds-1", UserArchitectureResourceType.RDS, "DB")
				),
				List.of(connection("conn-1", "app-1", "rds-1", UserArchitectureConnectionType.REQUEST_FLOW))
		);
		UserArchitecture target = architecture(
				"target",
				"대상",
				List.of(
						node("app-1", UserArchitectureResourceType.AUTO_SCALING_GROUP, "API 그룹"),
						node("rds-1", UserArchitectureResourceType.RDS, "DB")
				),
				List.of(connection("conn-1", "rds-1", "app-1", UserArchitectureConnectionType.REQUEST_FLOW))
		);

		UserArchitectureComparisonResult result = UserArchitectureComparator.compare(base, target);

		assertThat(result.resources().changed())
				.extracting(UserArchitectureComparisonResult.ResourceChange::resourceId)
				.containsExactly("app-1");
		assertThat(result.connections().changed())
				.extracting(UserArchitectureComparisonResult.ConnectionChange::connectionId)
				.containsExactly("conn-1");
	}

	@Test
	void 시나리오_권장_구조와_사용자_아키텍처의_누락_불필요_컴포넌트와_trade_off를_비교한다() {
		ScenarioOption coreOption = ScenarioOption.newOptionWithGraphKey(
				"add-alb-auto-scaling",
				"ALB와 Auto Scaling 추가",
				"정상 인스턴스로 요청을 분산합니다.",
				3,
				true,
				0,
				new TradeOffEffects(3, 3, -2, -2, 0, 1)
		);
		Scenario scenario = new Scenario(
				1L,
				"single-spring-boot",
				"단일 Spring Boot 배포",
				ScenarioCategory.COMPUTE,
				ScenarioLevel.BEGINNER,
				"단일 장애 지점을 줄입니다.",
				"설명",
				List.of("Client", "EC2", "RDS"),
				List.of(coreOption)
		);
		UserArchitecture userArchitecture = architecture(
				"user",
				"사용자 구조",
				List.of(
						node("client", UserArchitectureResourceType.CLIENT, "Client"),
						node("ec2", UserArchitectureResourceType.EC2, "EC2"),
						node("rds", UserArchitectureResourceType.RDS, "RDS"),
						node("redis", UserArchitectureResourceType.REDIS, "Redis")
				),
				List.of(
						connection("conn-1", "client", "ec2", UserArchitectureConnectionType.REQUEST_FLOW),
						connection("conn-2", "ec2", "rds", UserArchitectureConnectionType.REQUEST_FLOW)
				)
		);

		UserArchitectureComparisonResult result = UserArchitectureComparator.compareWithScenarioRecommendation(
				userArchitecture,
				scenario,
				List.of(coreOption)
		);

		assertThat(result.scenarioComparison().missingRecommendedResources())
				.extracting(UserArchitectureComparisonResult.ResourceChange::baseResourceType)
				.contains(UserArchitectureResourceType.ALB, UserArchitectureResourceType.AUTO_SCALING_GROUP);
		assertThat(result.scenarioComparison().extraResources())
				.extracting(UserArchitectureComparisonResult.ResourceChange::targetResourceType)
				.containsExactly(UserArchitectureResourceType.REDIS);
		assertThat(result.scenarioComparison().learningImpacts())
				.extracting(UserArchitectureComparisonResult.LearningImpact::code)
				.contains("RECOMMENDED_RESOURCE_MISSING", "EXTRA_RESOURCE_PRESENT");
		assertThat(result.tradeOffReferences())
				.extracting(UserArchitectureComparisonResult.TradeOffReference::optionName)
				.containsExactly("ALB와 Auto Scaling 추가");
	}

	private static UserArchitecture architecture(
			String id,
			String title,
			List<UserArchitectureNode> nodes,
			List<UserArchitectureConnection> connections
	) {
		return new UserArchitecture(id, title, "", NOW, NOW, nodes, connections);
	}

	private static UserArchitectureNode node(String id, UserArchitectureResourceType resourceType, String displayName) {
		return new UserArchitectureNode(id, resourceType, displayName);
	}

	private static UserArchitectureConnection connection(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		return new UserArchitectureConnection(id, sourceNodeId, targetNodeId, connectionType);
	}
}
