package com.yunhwan.cloudsimlab.userarchitecture.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftArchitecture;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftConnection;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureValidator.DraftNode;

class UserArchitectureValidatorTests {

	@Test
	void 구조_오류와_지원하지_않는_타입을_ERROR로_반환한다() {
		UserArchitectureValidationResult result = UserArchitectureValidator.validate(new DraftArchitecture(
				List.of(
						new DraftNode("ec2-1", "EC2", "API 서버"),
						new DraftNode("ec2-1", "EC2", "API 서버 복제"),
						new DraftNode("legacy-1", "LEGACY_DB", "지원하지 않는 DB")
				),
				List.of(
						new DraftConnection("conn-1", "ec2-1", "missing", "REQUEST_FLOW"),
						new DraftConnection("conn-2", "ec2-1", "ec2-1", "REQUEST_FLOW"),
						new DraftConnection("conn-3", "ec2-1", "ec2-1", "CUSTOM")
				)
		));

		assertThat(result.valid()).isFalse();
		assertThat(result.errors())
				.extracting(UserArchitectureValidationIssue::code)
				.containsExactly(
						"DUPLICATE_NODE_ID",
						"UNSUPPORTED_RESOURCE_TYPE",
						"MISSING_CONNECTION_TARGET",
						"SELF_LOOP_CONNECTION",
						"UNSUPPORTED_CONNECTION_TYPE"
				);
		assertThat(result.guidance())
				.extracting(UserArchitectureValidationIssue::code)
				.containsExactly("FIX_STRUCTURE_FIRST");
	}

	@Test
	void 카탈로그_기준으로_명백히_맞지_않는_연결을_ERROR로_반환한다() {
		UserArchitectureValidationResult result = UserArchitectureValidator.validate(new DraftArchitecture(
				List.of(
						new DraftNode("client-1", "CLIENT", "Client"),
						new DraftNode("ec2-1", "EC2", "API 서버"),
						new DraftNode("rds-1", "RDS", "DB")
				),
				List.of(
						new DraftConnection("conn-1", "client-1", "ec2-1", "REPLICATION"),
						new DraftConnection("conn-2", "ec2-1", "rds-1", "SECURITY_RULE")
				)
		));

		assertThat(result.valid()).isFalse();
		assertThat(result.errors())
				.extracting(UserArchitectureValidationIssue::code)
				.containsExactly("INVALID_REPLICATION_CONNECTION", "INVALID_SECURITY_RULE_CONNECTION");
	}

	@Test
	void 최소_운영_위험과_학습_안내를_WARNING과_GUIDANCE로_구분한다() {
		UserArchitectureValidationResult result = UserArchitectureValidator.validate(new DraftArchitecture(
				List.of(
						new DraftNode("client-1", "CLIENT", "Client"),
						new DraftNode("alb-1", "ALB", "ALB"),
						new DraftNode("ec2-1", "EC2", "API 서버"),
						new DraftNode("rds-1", "RDS", "DB"),
						new DraftNode("private-subnet-1", "SUBNET", "Private subnet")
				),
				List.of(
						new DraftConnection("conn-1", "client-1", "rds-1", "REQUEST_FLOW"),
						new DraftConnection("conn-2", "private-subnet-1", "ec2-1", "DEPENDS_ON")
				)
		));

		assertThat(result.valid()).isTrue();
		assertThat(result.errors()).isEmpty();
		assertThat(result.warnings())
				.extracting(UserArchitectureValidationIssue::code)
				.containsExactly(
						"CLIENT_DIRECT_RDS_ACCESS",
						"PRIVATE_SUBNET_OUTBOUND_MISSING",
						"PRIVATE_SUBNET_ENTRY_MISSING",
						"ALB_TARGET_MISSING",
						"APPLICATION_DATA_PATH_MISSING"
				);
		assertThat(result.guidance())
				.extracting(UserArchitectureValidationIssue::code)
				.containsExactly("SECURITY_BOUNDARY_REVIEW");
	}

	@Test
	void ALB가_Target_Group으로_전달하면_Private_subnet_진입_경고를_내지_않는다() {
		UserArchitectureValidationResult result = UserArchitectureValidator.validate(new DraftArchitecture(
				List.of(
						new DraftNode("alb-1", "ALB", "ALB"),
						new DraftNode("target-group-1", "TARGET_GROUP", "Target Group"),
						new DraftNode("private-subnet-1", "SUBNET", "Private subnet"),
						new DraftNode("ec2-1", "EC2", "API 서버"),
						new DraftNode("nat-1", "NAT_GATEWAY", "NAT Gateway")
				),
				List.of(
						new DraftConnection("conn-1", "alb-1", "target-group-1", "REQUEST_FLOW"),
						new DraftConnection("conn-2", "target-group-1", "ec2-1", "REQUEST_FLOW"),
						new DraftConnection("conn-3", "private-subnet-1", "ec2-1", "DEPENDS_ON"),
						new DraftConnection("conn-4", "private-subnet-1", "nat-1", "NETWORK_ROUTE")
				)
		));

		assertThat(result.warnings())
				.extracting(UserArchitectureValidationIssue::code)
				.doesNotContain("PRIVATE_SUBNET_ENTRY_MISSING", "ALB_TARGET_MISSING");
	}

	@Test
	void 데이터_저장소_도달성은_DEPENDS_ON이_아닌_REQUEST_FLOW만_인정한다() {
		UserArchitectureValidationResult result = UserArchitectureValidator.validate(new DraftArchitecture(
				List.of(
						new DraftNode("ec2-1", "EC2", "API 서버"),
						new DraftNode("rds-1", "RDS", "DB")
				),
				List.of(new DraftConnection("conn-1", "ec2-1", "rds-1", "DEPENDS_ON"))
		));

		assertThat(result.warnings())
				.extracting(UserArchitectureValidationIssue::code)
				.contains("APPLICATION_DATA_PATH_MISSING");
	}

	@Test
	void 같은_입력은_항상_같은_검증_결과를_반환한다() {
		DraftArchitecture architecture = new DraftArchitecture(
				List.of(
						new DraftNode("alb-1", "ALB", "ALB"),
						new DraftNode("ec2-1", "EC2", "API 서버")
				),
				List.of(new DraftConnection("conn-1", "alb-1", "ec2-1", "REQUEST_FLOW"))
		);

		UserArchitectureValidationResult first = UserArchitectureValidator.validate(architecture);
		UserArchitectureValidationResult second = UserArchitectureValidator.validate(architecture);

		assertThat(first).isEqualTo(second);
	}
}
