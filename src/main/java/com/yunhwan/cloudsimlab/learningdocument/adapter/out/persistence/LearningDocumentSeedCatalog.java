package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;

public final class LearningDocumentSeedCatalog {

	private static final List<SeedDocument> DOCUMENTS = List.of(
				new SeedDocument(
						"ec2-compute-capacity",
						"EC2와 컴퓨팅 용량",
						DocumentCategory.EC2,
						DocumentLevel.BEGINNER,
						"EC2 용량 선택이 성능, 비용, 장애 영향에 어떤 판단 기준을 만드는지 이해합니다.",
						"ec2-compute-capacity.md",
						1,
						List.of(),
						List.of("EC2", "compute", "capacity", "availability"),
						List.of("single-server-deployment"),
						List.of("single-spring-boot", "traffic-spike-compute")
				),
				new SeedDocument(
						"private-subnet-application-server",
						"Private subnet과 애플리케이션 서버",
						DocumentCategory.SUBNET,
						DocumentLevel.BEGINNER,
						"애플리케이션 서버를 인터넷에 직접 노출하지 않는 네트워크 판단 기준을 이해합니다.",
						"private-subnet-application-server.md",
						2,
						List.of("ec2-compute-capacity"),
						List.of("VPC", "subnet", "private-subnet", "network"),
						List.of("network-boundary", "alb-private-subnet"),
						List.of("private-subnet-app", "private-subnet-nat-missing")
				),
				new SeedDocument(
						"security-group-least-privilege",
						"Security Group 최소 허용",
						DocumentCategory.SECURITY_GROUP,
						DocumentLevel.BEGINNER,
						"필요한 포트와 출발지만 허용해 공격 표면을 줄이는 기준을 이해합니다.",
						"security-group-least-privilege.md",
						3,
						List.of("private-subnet-application-server"),
						List.of("security-group", "least-privilege", "network", "security"),
						List.of("network-boundary", "user-architecture-practice"),
						List.of("private-subnet-app", "alb-health-check-failure", "security-group-misconfiguration")
				),
				new SeedDocument(
						"alb-traffic-distribution",
						"ALB와 트래픽 분산",
						DocumentCategory.ALB,
						DocumentLevel.BEGINNER,
						"ALB가 트래픽 분산, 배포 안정성, 장애 우회에 주는 효과를 이해합니다.",
						"alb-traffic-distribution.md",
						4,
						List.of("private-subnet-application-server", "security-group-least-privilege"),
						List.of("ALB", "load-balancing", "health-check", "availability"),
						List.of("alb-private-subnet", "auto-scaling-health-check"),
						List.of("single-spring-boot", "private-subnet-app", "traffic-spike-compute", "alb-health-check-failure", "security-group-misconfiguration")
				),
				new SeedDocument(
						"auto-scaling-basics",
						"Auto Scaling 기본",
						DocumentCategory.AUTO_SCALING,
						DocumentLevel.INTERMEDIATE,
						"트래픽 변화에 맞춰 EC2 수를 조절할 때 필요한 지표와 한계를 이해합니다.",
						"auto-scaling-basics.md",
						5,
						List.of("ec2-compute-capacity", "alb-traffic-distribution"),
						List.of("autoscaling", "EC2", "capacity", "availability"),
						List.of("auto-scaling-health-check"),
						List.of("single-spring-boot", "traffic-spike-compute", "rds-connection-pool-exhaustion")
				),
				new SeedDocument(
						"nat-gateway-outbound-communication",
						"NAT Gateway와 아웃바운드 통신",
						DocumentCategory.NAT_GATEWAY,
						DocumentLevel.INTERMEDIATE,
						"Private subnet 서버가 외부로 나가야 할 때 NAT Gateway의 역할과 비용을 이해합니다.",
						"nat-gateway-outbound-communication.md",
						6,
						List.of("private-subnet-application-server"),
						List.of("NAT Gateway", "private-subnet", "outbound", "cost"),
						List.of("user-architecture-practice"),
						List.of("private-subnet-app", "private-subnet-nat-missing")
				),
				new SeedDocument(
						"rds-connection-management",
						"RDS와 연결 관리",
						DocumentCategory.RDS,
						DocumentLevel.BEGINNER,
						"RDS 병목을 판단할 때 연결 수, 쿼리 시간, 커넥션 풀 설정을 함께 이해합니다.",
						"rds-connection-management.md",
						7,
						List.of("ec2-compute-capacity", "security-group-least-privilege"),
						List.of("RDS", "connection-pool", "concurrency", "database"),
						List.of("data-tier-scaling"),
						List.of("rds-failure", "read-heavy-performance", "redis-failure-fallback", "rds-connection-pool-exhaustion")
				),
				new SeedDocument(
						"rds-multi-az",
						"RDS Multi-AZ",
						DocumentCategory.RDS,
						DocumentLevel.INTERMEDIATE,
						"RDS 장애 시 자동 장애 조치가 필요한 상황과 비용 trade-off를 이해합니다.",
						"rds-multi-az.md",
						8,
						List.of("rds-connection-management"),
						List.of("RDS", "multi-az", "availability", "failover"),
						List.of("data-tier-scaling"),
						List.of("rds-failure")
				),
				new SeedDocument(
						"read-replica-read-scaling",
						"Read Replica와 읽기 확장",
						DocumentCategory.READ_REPLICA,
						DocumentLevel.INTERMEDIATE,
						"조회 트래픽을 분산할 때 Read Replica가 해결하는 문제와 한계를 이해합니다.",
						"read-replica-read-scaling.md",
						9,
						List.of("rds-connection-management"),
						List.of("read-replica", "RDS", "read-scaling", "consistency"),
						List.of("data-tier-scaling"),
						List.of("rds-failure", "read-heavy-performance")
				),
				new SeedDocument(
						"redis-cache",
						"Redis Cache",
						DocumentCategory.REDIS,
						DocumentLevel.INTERMEDIATE,
						"반복 조회를 캐시할 때 성능 개선과 데이터 일관성 위험을 함께 이해합니다.",
						"redis-cache.md",
						10,
						List.of("rds-connection-management"),
						List.of("Redis", "cache", "consistency", "availability"),
						List.of("data-tier-scaling"),
						List.of("read-heavy-performance", "redis-failure-fallback")
				)
	);

	private static final Set<String> DOCUMENT_KEYS = DOCUMENTS.stream()
			.map(SeedDocument::documentKey)
			.collect(Collectors.toUnmodifiableSet());

	private LearningDocumentSeedCatalog() {
	}

	public static List<SeedDocument> documents() {
		return DOCUMENTS;
	}

	public static Set<String> documentKeys() {
		return DOCUMENT_KEYS;
	}

	public record SeedDocument(
			String documentKey,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String contentFileName,
			int orderIndex,
			List<String> prerequisiteDocumentIds,
			List<String> conceptTags,
			List<String> relatedModuleIds,
			List<String> relatedScenarioIds
	) {
		public SeedDocument {
			documentKey = requireText(documentKey, "documentKey");
			title = requireText(title, "title");
			category = Objects.requireNonNull(category, "Learning document seed category must not be null");
			level = Objects.requireNonNull(level, "Learning document seed level must not be null");
			summary = requireText(summary, "summary");
			contentFileName = requireText(contentFileName, "contentFileName");
			prerequisiteDocumentIds = List.copyOf(Objects.requireNonNull(prerequisiteDocumentIds, "Learning document seed prerequisiteDocumentIds must not be null"));
			conceptTags = List.copyOf(Objects.requireNonNull(conceptTags, "Learning document seed conceptTags must not be null"));
			relatedModuleIds = List.copyOf(Objects.requireNonNull(relatedModuleIds, "Learning document seed relatedModuleIds must not be null"));
			relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Learning document seed relatedScenarioIds must not be null"));
		}

		private static String requireText(String value, String fieldName) {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException("Learning document seed " + fieldName + " must not be blank");
			}
			return value;
		}
	}
}
