package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;

public final class LearningDocumentSeedCatalog {

	private LearningDocumentSeedCatalog() {
	}

	public static List<SeedDocument> documents() {
		return List.of(
				new SeedDocument(
						"ec2-compute-capacity",
						"EC2와 컴퓨팅 용량",
						DocumentCategory.COMPUTE,
						DocumentLevel.BEGINNER,
						"EC2 용량 선택이 성능, 비용, 장애 영향에 어떤 판단 기준을 만드는지 이해합니다.",
						"ec2-compute-capacity.md"
				),
				new SeedDocument(
						"private-subnet-application-server",
						"Private subnet과 애플리케이션 서버",
						DocumentCategory.NETWORK,
						DocumentLevel.BEGINNER,
						"애플리케이션 서버를 인터넷에 직접 노출하지 않는 네트워크 판단 기준을 이해합니다.",
						"private-subnet-application-server.md"
				),
				new SeedDocument(
						"alb-traffic-distribution",
						"ALB와 트래픽 분산",
						DocumentCategory.NETWORK,
						DocumentLevel.BEGINNER,
						"ALB가 트래픽 분산, 배포 안정성, 장애 우회에 주는 효과를 이해합니다.",
						"alb-traffic-distribution.md"
				),
				new SeedDocument(
						"auto-scaling-basics",
						"Auto Scaling 기본",
						DocumentCategory.COMPUTE,
						DocumentLevel.INTERMEDIATE,
						"트래픽 변화에 맞춰 EC2 수를 조절할 때 필요한 지표와 한계를 이해합니다.",
						"auto-scaling-basics.md"
				),
				new SeedDocument(
						"security-group-least-privilege",
						"Security Group 최소 허용",
						DocumentCategory.SECURITY,
						DocumentLevel.BEGINNER,
						"필요한 포트와 출발지만 허용해 공격 표면을 줄이는 기준을 이해합니다.",
						"security-group-least-privilege.md"
				),
				new SeedDocument(
						"rds-connection-management",
						"RDS와 연결 관리",
						DocumentCategory.STORAGE,
						DocumentLevel.BEGINNER,
						"RDS 병목을 판단할 때 연결 수, 쿼리 시간, 커넥션 풀 설정을 함께 이해합니다.",
						"rds-connection-management.md"
				),
				new SeedDocument(
						"rds-multi-az",
						"RDS Multi-AZ",
						DocumentCategory.STORAGE,
						DocumentLevel.INTERMEDIATE,
						"RDS 장애 시 자동 장애 조치가 필요한 상황과 비용 trade-off를 이해합니다.",
						"rds-multi-az.md"
				),
				new SeedDocument(
						"read-replica-read-scaling",
						"Read Replica와 읽기 확장",
						DocumentCategory.STORAGE,
						DocumentLevel.INTERMEDIATE,
						"조회 트래픽을 분산할 때 Read Replica가 해결하는 문제와 한계를 이해합니다.",
						"read-replica-read-scaling.md"
				),
				new SeedDocument(
						"redis-cache",
						"Redis Cache",
						DocumentCategory.COMPUTE,
						DocumentLevel.INTERMEDIATE,
						"반복 조회를 캐시할 때 성능 개선과 데이터 일관성 위험을 함께 이해합니다.",
						"redis-cache.md"
				),
				new SeedDocument(
						"nat-gateway-outbound-communication",
						"NAT Gateway와 아웃바운드 통신",
						DocumentCategory.NETWORK,
						DocumentLevel.INTERMEDIATE,
						"Private subnet 서버가 외부로 나가야 할 때 NAT Gateway의 역할과 비용을 이해합니다.",
						"nat-gateway-outbound-communication.md"
				)
		);
	}

	public static Set<String> documentKeys() {
		return documents().stream()
				.map(SeedDocument::documentKey)
				.collect(Collectors.toUnmodifiableSet());
	}

	public record SeedDocument(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String contentFileName
	) {
		public SeedDocument {
			documentKey = requireText(documentKey, "documentKey");
			title = requireText(title, "title");
			category = Objects.requireNonNull(category, "Learning document seed category must not be null");
			level = Objects.requireNonNull(level, "Learning document seed level must not be null");
			summary = requireText(summary, "summary");
			contentFileName = requireText(contentFileName, "contentFileName");
		}

		private static String requireText(String value, String fieldName) {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException("Learning document seed " + fieldName + " must not be blank");
			}
			return value;
		}
	}
}
