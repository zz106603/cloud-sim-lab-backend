package com.yunhwan.cloudsimlab.architecturepractice.adapter.out.persistence;

import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType.DEPENDS_ON;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType.NETWORK_ROUTE;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType.REPLICATION;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType.REQUEST_FLOW;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType.SECURITY_RULE;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.ALB;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.CLIENT;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.EC2;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.RDS;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.READ_REPLICA;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.REDIS;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.SECURITY_GROUP;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.SUBNET;
import static com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType.VPC;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeConnection;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeLevel;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeNode;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureConnectionType;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitectureResourceType;

public final class ArchitecturePracticeSeedCatalog {

	private static final List<ArchitecturePracticeTemplate> PRACTICES = List.of(
			new ArchitecturePracticeTemplate(
					"architecture-builder-basic",
					"단일 EC2 배포",
					"Client, EC2, RDS의 최소 요청 흐름을 직접 구성하며 단일 서버 구조의 한계를 확인합니다.",
					ArchitecturePracticeLevel.BEGINNER,
					"단일 EC2 배포 구조에서 요청 처리 계층과 데이터 저장소 의존 관계를 분리해 표현합니다.",
					List.of(
							"Client 요청이 EC2 애플리케이션 서버로 들어가는 흐름을 연결합니다.",
							"EC2가 RDS에 의존하는 데이터 접근 흐름을 표현합니다.",
							"단일 EC2 장애 시 어떤 리소스가 대체되지 않는지 확인합니다."
					),
					List.of(
							node("client", CLIENT, "Client"),
							node("ec2", EC2, "Spring Boot API"),
							node("rds", RDS, "Primary RDS")
					),
					List.of(
							connection("client-to-ec2", "client", "ec2", REQUEST_FLOW),
							connection("ec2-to-rds", "ec2", "rds", REQUEST_FLOW)
					),
					List.of(CLIENT, EC2, RDS),
					List.of(REQUEST_FLOW),
					List.of("ec2-compute-capacity", "rds-connection-management"),
					List.of("single-spring-boot"),
					List.of("single-server-deployment", "user-architecture-practice")
			),
			new ArchitecturePracticeTemplate(
					"alb-private-subnet-application",
					"ALB + private subnet 애플리케이션",
					"외부 진입점은 ALB로 제한하고 애플리케이션 서버와 RDS는 private 영역에 배치하는 구조를 연습합니다.",
					ArchitecturePracticeLevel.BEGINNER,
					"ALB, subnet, Security Group을 분리해 외부 노출을 줄이는 요청 경로를 구성합니다.",
					List.of(
							"Client는 ALB로만 요청을 보낼 수 있게 구성합니다.",
							"ALB에서 private subnet의 애플리케이션 서버로 이어지는 흐름을 연결합니다.",
							"Security Group으로 ALB, EC2, RDS 사이의 최소 허용 관계를 표시합니다."
					),
					List.of(
							node("client", CLIENT, "Client"),
							node("vpc", VPC, "서비스 VPC"),
							node("public-subnet", SUBNET, "Public subnet"),
							node("private-subnet", SUBNET, "Private app subnet"),
							node("alb", ALB, "Public ALB"),
							node("app-ec2", EC2, "Private API 서버"),
							node("rds", RDS, "Private RDS"),
							node("security-group", SECURITY_GROUP, "최소 허용 Security Group")
					),
					List.of(
							connection("client-to-alb", "client", "alb", REQUEST_FLOW),
							connection("alb-to-app", "alb", "app-ec2", REQUEST_FLOW),
							connection("app-to-rds", "app-ec2", "rds", REQUEST_FLOW),
							connection("subnet-in-vpc", "public-subnet", "vpc", NETWORK_ROUTE),
							connection("security-boundary", "security-group", "app-ec2", SECURITY_RULE)
					),
					List.of(CLIENT, VPC, SUBNET, ALB, EC2, RDS, SECURITY_GROUP),
					List.of(REQUEST_FLOW, NETWORK_ROUTE, SECURITY_RULE),
					List.of("alb-traffic-distribution", "private-subnet-application-server", "security-group-least-privilege"),
					List.of("private-subnet-app", "security-group-misconfiguration", "alb-health-check-failure"),
					List.of("alb-private-subnet", "user-architecture-practice")
			),
			new ArchitecturePracticeTemplate(
					"read-heavy-scaling-practice",
					"조회 부하 대응 데이터 계층",
					"Read Replica와 Redis 중 어떤 계층이 조회 부하를 줄이고 어떤 정합성 위험을 만드는지 비교하며 구성합니다.",
					ArchitecturePracticeLevel.INTERMEDIATE,
					"조회 중심 트래픽에서 캐시와 읽기 복제본을 요청 경로와 복제 관계로 구분합니다.",
					List.of(
							"기본 Client-ALB-EC2-RDS 요청 흐름을 유지합니다.",
							"반복 조회 경로에는 Redis를, DB 읽기 분산에는 Read Replica를 배치합니다.",
							"RDS에서 Read Replica로 이어지는 복제 관계와 정합성 주의점을 확인합니다."
					),
					List.of(
							node("client", CLIENT, "Client"),
							node("alb", ALB, "ALB"),
							node("app-ec2", EC2, "API 서버"),
							node("redis", REDIS, "Redis Cache"),
							node("rds", RDS, "Primary RDS"),
							node("read-replica", READ_REPLICA, "Read Replica")
					),
					List.of(
							connection("client-to-alb", "client", "alb", REQUEST_FLOW),
							connection("alb-to-app", "alb", "app-ec2", REQUEST_FLOW),
							connection("app-to-redis", "app-ec2", "redis", REQUEST_FLOW),
							connection("app-to-rds", "app-ec2", "rds", REQUEST_FLOW),
							connection("app-to-replica", "app-ec2", "read-replica", REQUEST_FLOW),
							connection("rds-to-replica", "rds", "read-replica", REPLICATION),
							connection("app-depends-on-cache", "app-ec2", "redis", DEPENDS_ON)
					),
					List.of(CLIENT, ALB, EC2, REDIS, RDS, READ_REPLICA),
					List.of(REQUEST_FLOW, REPLICATION, DEPENDS_ON),
					List.of("read-replica-read-scaling", "redis-cache", "rds-connection-management"),
					List.of("read-heavy-performance", "redis-failure-fallback"),
					List.of("data-tier-scaling", "user-architecture-practice")
			)
	);

	private static final Set<String> PRACTICE_IDS = PRACTICES.stream()
			.map(ArchitecturePracticeTemplate::id)
			.collect(Collectors.toUnmodifiableSet());

	private ArchitecturePracticeSeedCatalog() {
	}

	public static List<ArchitecturePracticeTemplate> practices() {
		return PRACTICES;
	}

	public static Set<String> practiceIds() {
		return PRACTICE_IDS;
	}

	private static ArchitecturePracticeNode node(String id, UserArchitectureResourceType resourceType, String displayName) {
		return new ArchitecturePracticeNode(id, resourceType, displayName);
	}

	private static ArchitecturePracticeConnection connection(
			String id,
			String sourceNodeId,
			String targetNodeId,
			UserArchitectureConnectionType connectionType
	) {
		return new ArchitecturePracticeConnection(id, sourceNodeId, targetNodeId, connectionType);
	}
}
