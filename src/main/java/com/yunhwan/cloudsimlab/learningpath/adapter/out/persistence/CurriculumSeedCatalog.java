package com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

public final class CurriculumSeedCatalog {

	public static final String BEGINNER_PATH_ID = "backend-aws-foundation";

	private static final List<LearningModule> MODULES = List.of(
			new LearningModule(
					"single-server-deployment",
					BEGINNER_PATH_ID,
					"단일 서버 배포",
					"EC2 한 대에 Spring Boot API를 배포할 때 생기는 용량, 배포 중단, 단일 장애 지점을 이해합니다.",
					List.of("EC2가 애플리케이션 실행 단위로 어떤 책임을 갖는지 설명한다.", "단일 서버 구조의 장애 지점과 확장 한계를 판단한다."),
					List.of("HTTP 요청 흐름", "Spring Boot 배포 기본"),
					1,
					List.of("ec2-compute-capacity"),
					List.of("single-spring-boot"),
					List.of()
			),
			new LearningModule(
					"network-boundary",
					BEGINNER_PATH_ID,
					"VPC/subnet/Security Group 네트워크 경계",
					"Public/Private subnet과 Security Group으로 외부 노출 범위를 줄이는 기준을 학습합니다.",
					List.of("인터넷 진입점과 내부 애플리케이션 서버를 구분한다.", "Security Group 최소 허용 원칙으로 요청 경로를 설명한다."),
					List.of("단일 서버 배포"),
					2,
					List.of("private-subnet-application-server", "security-group-least-privilege"),
					List.of("private-subnet-app", "security-group-misconfiguration"),
					List.of()
			),
			new LearningModule(
					"alb-private-subnet",
					BEGINNER_PATH_ID,
					"ALB와 private subnet 분리",
					"Client 진입점은 ALB로 유지하고 애플리케이션 서버는 private subnet에 두는 구조를 학습합니다.",
					List.of("ALB가 외부 요청 진입점과 장애 우회에 주는 효과를 설명한다.", "Private subnet 애플리케이션 서버의 인바운드 경계를 판단한다."),
					List.of("VPC/subnet/Security Group 네트워크 경계"),
					3,
					List.of("alb-traffic-distribution", "private-subnet-application-server"),
					List.of("private-subnet-app", "alb-health-check-failure"),
					List.of()
			),
			new LearningModule(
					"auto-scaling-health-check",
					BEGINNER_PATH_ID,
					"Auto Scaling과 Health Check",
					"트래픽 증가와 인스턴스 장애를 흡수하기 위해 ALB, Target Group, Auto Scaling을 함께 판단합니다.",
					List.of("Auto Scaling이 처리량과 가용성에 주는 효과를 설명한다.", "Health Check 실패가 요청 경로에 주는 영향을 해석한다."),
					List.of("ALB와 private subnet 분리"),
					4,
					List.of("auto-scaling-basics", "alb-traffic-distribution"),
					List.of("traffic-spike-compute", "alb-health-check-failure"),
					List.of()
			),
			new LearningModule(
					"data-tier-scaling",
					BEGINNER_PATH_ID,
					"RDS, Read Replica, Redis 데이터 계층",
					"데이터 저장소 병목과 장애를 성능, 가용성, 정합성 trade-off로 비교합니다.",
					List.of("RDS 장애 대응과 읽기 확장 선택지를 구분한다.", "Redis 캐시와 Read Replica의 정합성 위험을 설명한다."),
					List.of("Auto Scaling과 Health Check"),
					5,
					List.of("rds-connection-management", "rds-multi-az", "read-replica-read-scaling", "redis-cache"),
					List.of("rds-failure", "read-heavy-performance", "redis-failure-fallback", "rds-connection-pool-exhaustion"),
					List.of()
			),
			new LearningModule(
					"user-architecture-practice",
					BEGINNER_PATH_ID,
					"사용자 작성 아키텍처 연습",
					"학습한 리소스와 연결 타입으로 직접 아키텍처를 작성하고 검증 결과를 해석합니다.",
					List.of("리소스와 연결 관계를 분리해 시각화 가능한 구조로 표현한다.", "검증 결과의 ERROR/WARNING/GUIDANCE를 학습 피드백으로 해석한다."),
					List.of("RDS, Read Replica, Redis 데이터 계층"),
					6,
					List.of("nat-gateway-outbound-communication", "security-group-least-privilege"),
					List.of("private-subnet-nat-missing", "security-group-misconfiguration"),
					List.of("architecture-builder-basic")
			)
	);

	private static final List<LearningPath> PATHS = List.of(
			new LearningPath(
					BEGINNER_PATH_ID,
					"백엔드 개발자를 위한 AWS 운영 기초",
					"단일 EC2 배포에서 시작해 네트워크 경계, 트래픽 분산, 데이터 계층, 직접 작성한 아키텍처 검증까지 이어지는 입문 경로입니다.",
					"BEGINNER",
					"백엔드 운영 상황에서 AWS 아키텍처 선택이 성능, 비용, 안정성, 보안에 주는 영향을 단계적으로 판단합니다.",
					true,
					1,
					MODULES.stream()
							.sorted(Comparator.comparingInt(LearningModule::orderIndex))
							.map(LearningModule::id)
							.toList()
			)
	);

	private static final Set<String> PATH_IDS = PATHS.stream()
			.map(LearningPath::id)
			.collect(Collectors.toUnmodifiableSet());

	private static final Set<String> MODULE_IDS = MODULES.stream()
			.map(LearningModule::id)
			.collect(Collectors.toUnmodifiableSet());

	private CurriculumSeedCatalog() {
	}

	public static List<LearningPath> paths() {
		return PATHS;
	}

	public static List<LearningModule> modules() {
		return MODULES;
	}

	public static Set<String> pathIds() {
		return PATH_IDS;
	}

	public static Set<String> moduleIds() {
		return MODULE_IDS;
	}

	public static List<String> moduleIdsForDocument(String documentKey) {
		return MODULES.stream()
				.filter(module -> module.documentIds().contains(documentKey))
				.sorted(Comparator.comparingInt(LearningModule::orderIndex))
				.map(LearningModule::id)
				.toList();
	}

	public static List<String> moduleIdsForScenario(String scenarioGraphKey) {
		return MODULES.stream()
				.filter(module -> module.relatedScenarioIds().contains(scenarioGraphKey))
				.sorted(Comparator.comparingInt(LearningModule::orderIndex))
				.map(LearningModule::id)
				.toList();
	}
}
