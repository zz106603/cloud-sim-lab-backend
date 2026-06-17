package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentCheckpoint;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocumentRecallQuestion;

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
			List<String> relatedScenarioIds,
			List<LearningDocumentCheckpoint> checkpoints,
			List<LearningDocumentRecallQuestion> recallQuestions
	) {
		public SeedDocument(
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
			this(
					documentKey,
					title,
					category,
					level,
					summary,
					contentFileName,
					orderIndex,
					prerequisiteDocumentIds,
					conceptTags,
					relatedModuleIds,
					relatedScenarioIds,
					defaultCheckpoints(documentKey),
					defaultRecallQuestions(documentKey)
			);
		}

		public SeedDocument {
			title = requireText(title, "title");
			category = Objects.requireNonNull(category, "Learning document seed category must not be null");
			level = Objects.requireNonNull(level, "Learning document seed level must not be null");
			summary = requireText(summary, "summary");
			contentFileName = requireText(contentFileName, "contentFileName");
			prerequisiteDocumentIds = List.copyOf(Objects.requireNonNull(prerequisiteDocumentIds, "Learning document seed prerequisiteDocumentIds must not be null"));
			conceptTags = List.copyOf(Objects.requireNonNull(conceptTags, "Learning document seed conceptTags must not be null"));
			relatedModuleIds = List.copyOf(Objects.requireNonNull(relatedModuleIds, "Learning document seed relatedModuleIds must not be null"));
			relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Learning document seed relatedScenarioIds must not be null"));
			checkpoints = List.copyOf(Objects.requireNonNull(checkpoints, "Learning document seed checkpoints must not be null"));
			recallQuestions = List.copyOf(Objects.requireNonNull(recallQuestions, "Learning document seed recallQuestions must not be null"));
		}

		private static String requireText(String value, String fieldName) {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException("Learning document seed " + fieldName + " must not be blank");
			}
			return value;
		}
	}

	private static List<LearningDocumentCheckpoint> defaultCheckpoints(String documentKey) {
		if (documentKey == null) {
			return List.of();
		}
		return switch (documentKey) {
			case "ec2-compute-capacity" -> List.of(
					checkpoint("ec2-cp-capacity", "EC2 크기 증설은 빠른 성능 개선을 줄 수 있지만 단일 장애 지점과 비용 문제를 함께 남깁니다.", "performance", "availability", "cost"),
					checkpoint("ec2-cp-metric", "CPU만 보지 말고 응답 시간, 큐 대기, 장애 영향 범위를 함께 확인해야 합니다.", "performance", "complexity")
			);
			case "private-subnet-application-server" -> List.of(
					checkpoint("private-subnet-cp-exposure", "애플리케이션 서버를 private subnet에 두면 직접 노출을 줄이지만 외부 진입 경로를 별도로 설계해야 합니다.", "security", "complexity"),
					checkpoint("private-subnet-cp-routing", "Private subnet 서버의 업데이트와 외부 API 호출은 NAT 같은 아웃바운드 경로가 있을 때 가능합니다.", "availability", "cost", "complexity")
			);
			case "security-group-least-privilege" -> List.of(
					checkpoint("sg-cp-source", "Security Group은 포트뿐 아니라 허용 출발지를 좁혀야 최소 권한에 가까워집니다.", "security"),
					checkpoint("sg-cp-health-check", "ALB health check가 실패하면 애플리케이션 문제가 아니라 Security Group 허용 범위 문제일 수 있습니다.", "availability", "security")
			);
			case "alb-traffic-distribution" -> List.of(
					checkpoint("alb-cp-target", "ALB는 여러 대상에 요청을 분산하고 비정상 대상을 제외해 단일 EC2 장애 영향을 줄입니다.", "availability", "performance"),
					checkpoint("alb-cp-health", "Health check 경로와 Security Group이 맞지 않으면 정상 서버도 트래픽을 받지 못합니다.", "availability", "security", "complexity")
			);
			case "auto-scaling-basics" -> List.of(
					checkpoint("as-cp-signal", "Auto Scaling은 평균 CPU 같은 확장 신호가 실제 병목을 대표할 때 효과적입니다.", "performance", "complexity"),
					checkpoint("as-cp-delay", "Auto Scaling은 시작 지연이 있어 즉시 몰리는 트래픽을 완전히 흡수하지 못할 수 있습니다.", "availability", "cost")
			);
			case "nat-gateway-outbound-communication" -> List.of(
					checkpoint("nat-cp-outbound", "NAT Gateway는 private subnet 서버의 아웃바운드 통신을 열어 주지만 인바운드 노출을 만들지는 않습니다.", "security", "availability"),
					checkpoint("nat-cp-cost", "NAT Gateway는 고가용성을 높일수록 고정 비용과 데이터 처리 비용이 늘어납니다.", "availability", "cost", "complexity")
			);
			case "rds-connection-management" -> List.of(
					checkpoint("rds-conn-cp-pool", "커넥션 풀이 고갈되면 EC2를 늘려도 RDS 연결 한계 때문에 장애가 커질 수 있습니다.", "performance", "availability"),
					checkpoint("rds-conn-cp-query", "RDS 병목은 연결 수, 쿼리 시간, 인덱스, 트랜잭션 대기 시간을 함께 봐야 합니다.", "performance", "complexity")
			);
			case "rds-multi-az" -> List.of(
					checkpoint("multi-az-cp-failover", "RDS Multi-AZ는 장애 조치와 가용성을 높이지만 읽기 성능 확장 수단은 아닙니다.", "availability", "performance"),
					checkpoint("multi-az-cp-cost", "자동 장애 조치가 필요한 서비스일수록 Multi-AZ 비용을 장애 비용과 비교해야 합니다.", "availability", "cost")
			);
			case "read-replica-read-scaling" -> List.of(
					checkpoint("replica-cp-read", "Read Replica는 읽기 부하를 분산하지만 쓰기 병목을 직접 해결하지 않습니다.", "performance", "complexity"),
					checkpoint("replica-cp-lag", "복제 지연이 있는 데이터는 방금 쓴 값을 바로 읽어야 하는 화면에 부적합할 수 있습니다.", "consistency", "performance")
			);
			case "redis-cache" -> List.of(
					checkpoint("redis-cp-hit", "Redis Cache는 반복 조회 응답 시간을 줄이지만 캐시 무효화 기준이 없으면 오래된 데이터를 보여줄 수 있습니다.", "performance", "consistency"),
					checkpoint("redis-cp-fallback", "Redis 장애 시 DB fallback을 준비하지 않으면 캐시 장애가 전체 조회 장애로 번질 수 있습니다.", "availability", "complexity")
			);
			default -> List.of();
		};
	}

	private static List<LearningDocumentRecallQuestion> defaultRecallQuestions(String documentKey) {
		if (documentKey == null) {
			return List.of();
		}
		return switch (documentKey) {
			case "ec2-compute-capacity" -> List.of(
					recall("ec2-rq-single-point", "단일 EC2 크기만 키우면 어떤 문제는 남나요?", "처리량은 늘 수 있지만 단일 장애 지점, 배포 중단 위험, 비용 증가가 남습니다.", "single-spring-boot"),
					recall("ec2-rq-spike", "트래픽 급증에서 EC2 증설 판단 전에 어떤 지표를 확인해야 하나요?", "CPU, 응답 시간, 요청 대기, 병목 위치가 애플리케이션인지 데이터 계층인지 확인해야 합니다.", "traffic-spike-compute")
			);
			case "private-subnet-application-server" -> List.of(
					recall("private-subnet-rq-entry", "Private subnet 서버는 외부 요청을 어떻게 받아야 하나요?", "인터넷에 직접 노출하지 않고 ALB 같은 public 진입점을 통해 요청을 받아야 합니다.", "private-subnet-app"),
					recall("private-subnet-rq-nat", "Private subnet 서버가 패키지 업데이트에 실패하면 무엇을 의심해야 하나요?", "NAT Gateway나 라우팅 테이블 같은 아웃바운드 경로 누락을 의심해야 합니다.", "private-subnet-nat-missing")
			);
			case "security-group-least-privilege" -> List.of(
					recall("sg-rq-alb", "ALB 뒤 EC2 Security Group은 어떤 출발지를 허용해야 하나요?", "전체 인터넷이 아니라 ALB Security Group에서 오는 애플리케이션 포트만 허용해야 합니다.", "private-subnet-app"),
					recall("sg-rq-health", "ALB health check 실패 시 Security Group에서 무엇을 확인하나요?", "ALB가 health check 경로의 포트로 EC2에 접근할 수 있는지 확인합니다.", "alb-health-check-failure")
			);
			case "alb-traffic-distribution" -> List.of(
					recall("alb-rq-failure", "ALB가 단일 EC2 장애 영향을 줄이는 방식은 무엇인가요?", "Health check로 비정상 대상을 제외하고 정상 대상에만 트래픽을 분산합니다.", "single-spring-boot"),
					recall("alb-rq-health", "ALB가 정상 서버를 unhealthy로 볼 때 먼저 확인할 설정은 무엇인가요?", "Health check path, port, 응답 코드, Security Group 허용 범위를 확인합니다.", "alb-health-check-failure")
			);
			case "auto-scaling-basics" -> List.of(
					recall("as-rq-signal", "Auto Scaling 정책을 만들 때 확장 신호는 어떻게 골라야 하나요?", "실제 병목과 사용자 응답 지연을 잘 대표하는 지표를 선택해야 합니다.", "traffic-spike-compute"),
					recall("as-rq-pool", "EC2를 늘린 뒤 RDS 연결 장애가 커질 수 있는 이유는 무엇인가요?", "인스턴스 수 증가가 전체 커넥션 풀 수 증가로 이어져 RDS 연결 한계를 초과할 수 있습니다.", "rds-connection-pool-exhaustion")
			);
			case "nat-gateway-outbound-communication" -> List.of(
					recall("nat-rq-purpose", "NAT Gateway는 private subnet 서버에 어떤 통신을 제공하나요?", "서버가 인터넷으로 나가는 아웃바운드 통신을 제공하지만 외부에서 직접 들어오는 경로는 만들지 않습니다.", "private-subnet-app"),
					recall("nat-rq-missing", "Private subnet 서버의 외부 API 호출 실패에서 확인할 네트워크 요소는 무엇인가요?", "라우팅 테이블, NAT Gateway, 보안 정책, 대상 네트워크 접근 가능성을 확인합니다.", "private-subnet-nat-missing")
			);
			case "rds-connection-management" -> List.of(
					recall("rds-conn-rq-pool", "Connection Pool 고갈은 사용자 요청에 어떤 영향을 주나요?", "DB 연결을 기다리는 요청이 쌓여 응답 시간이 늘고 타임아웃이 발생할 수 있습니다.", "rds-connection-pool-exhaustion"),
					recall("rds-conn-rq-read-heavy", "조회 트래픽 증가에서 RDS 병목을 판단할 때 무엇을 함께 봐야 하나요?", "연결 수, CPU, 쿼리 시간, 느린 쿼리, 읽기/쓰기 비율을 함께 봐야 합니다.", "read-heavy-performance")
			);
			case "rds-multi-az" -> List.of(
					recall("multi-az-rq-purpose", "RDS Multi-AZ가 해결하는 핵심 문제는 무엇인가요?", "주 인스턴스 장애 시 자동 장애 조치로 가용성을 높이는 문제를 해결합니다.", "rds-failure"),
					recall("multi-az-rq-limit", "RDS Multi-AZ를 읽기 성능 개선책으로 보기 어려운 이유는 무엇인가요?", "대기 인스턴스는 장애 조치용이며 일반적인 읽기 분산 대상이 아니기 때문입니다.", "rds-failure")
			);
			case "read-replica-read-scaling" -> List.of(
					recall("replica-rq-fit", "Read Replica가 잘 맞는 트래픽 형태는 무엇인가요?", "쓰기보다 반복 조회가 많은 읽기 중심 트래픽에 잘 맞습니다.", "read-heavy-performance"),
					recall("replica-rq-lag", "Read Replica 사용 시 최신성이 중요한 화면에서 주의할 점은 무엇인가요?", "복제 지연 때문에 방금 변경한 데이터가 늦게 보일 수 있어 원본 DB 조회가 필요할 수 있습니다.", "read-heavy-performance")
			);
			case "redis-cache" -> List.of(
					recall("redis-rq-cache", "Redis Cache가 조회 성능을 개선하는 원리는 무엇인가요?", "반복 조회 결과를 메모리에 저장해 RDS 접근과 쿼리 시간을 줄입니다.", "read-heavy-performance"),
					recall("redis-rq-fallback", "Redis 장애 fallback을 준비해야 하는 이유는 무엇인가요?", "캐시 장애가 전체 조회 장애가 되지 않도록 DB 조회나 우회 경로를 유지해야 합니다.", "redis-failure-fallback")
			);
			default -> List.of();
		};
	}

	private static LearningDocumentCheckpoint checkpoint(String id, String keySentence, String... judgmentPerspectives) {
		return new LearningDocumentCheckpoint(id, keySentence, List.of(judgmentPerspectives));
	}

	private static LearningDocumentRecallQuestion recall(
			String id,
			String question,
			String expectedAnswer,
			String relatedScenarioId
	) {
		return new LearningDocumentRecallQuestion(id, question, expectedAnswer, relatedScenarioId);
	}
}
