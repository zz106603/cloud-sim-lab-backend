package com.yunhwan.cloudsimlab.learningrelation.domain;

import java.util.List;

public final class LearningRelations {

	private static final List<LearningRelation> RELATIONS = List.of(
			relation(
					"single-spring-boot",
					"ec2-compute-capacity",
					"단일 EC2의 용량 한계와 단일 장애 지점을 판단하는 데 필요한 문서입니다.",
					"scale-up과 scale-out이 용량과 장애 대응에 만드는 차이"
			),
			relation(
					"single-spring-boot",
					"alb-traffic-distribution",
					"단일 서버 장애와 배포 중단을 줄이는 요청 분산 구조를 이해하는 데 필요한 문서입니다.",
					"ALB Health Check와 장애 인스턴스 우회 흐름"
			),
			relation(
					"single-spring-boot",
					"auto-scaling-basics",
					"여러 EC2로 점진적으로 확장할 때 필요한 운영 조건을 이해하는 데 필요한 문서입니다.",
					"수평 확장의 효과와 시작 지연 및 비용 부담"
			),
			relation(
					"private-subnet-app",
					"private-subnet-application-server",
					"애플리케이션 서버의 직접 노출을 줄이는 네트워크 경계를 판단하는 데 필요한 문서입니다.",
					"Public 진입점과 Private 애플리케이션 서버의 요청 흐름"
			),
			relation(
					"private-subnet-app",
					"alb-traffic-distribution",
					"외부 요청을 받으면서 Private subnet의 EC2로 전달하는 진입점을 이해하는 데 필요한 문서입니다.",
					"ALB에서 Private EC2로 이어지는 요청 및 Health Check 흐름"
			),
			relation(
					"private-subnet-app",
					"security-group-least-privilege",
					"ALB, EC2, RDS 사이에 필요한 통신만 허용하는 기준을 판단하는 데 필요한 문서입니다.",
					"Security Group 참조 관계와 과도한 네트워크 노출 위험"
			),
			relation(
					"private-subnet-app",
					"nat-gateway-outbound-communication",
					"Private subnet 서버의 외부 통신 필요성과 비용을 판단하는 데 필요한 문서입니다.",
					"NAT Gateway 아웃바운드 경로와 비용 및 가용성 부담"
			),
			relation(
					"traffic-spike-compute",
					"ec2-compute-capacity",
					"트래픽 증가 시 애플리케이션 서버 계층이 병목인지 판단하는 데 필요한 문서입니다.",
					"EC2 지표를 이용한 병목 식별과 scale-up 한계"
			),
			relation(
					"traffic-spike-compute",
					"auto-scaling-basics",
					"트래픽 피크에 맞춰 EC2 수를 조절하는 기준을 이해하는 데 필요한 문서입니다.",
					"확장 지표, 시작 지연, 최대 용량과 비용 trade-off"
			),
			relation(
					"traffic-spike-compute",
					"alb-traffic-distribution",
					"확장된 EC2로 요청을 안전하게 분산하는 흐름을 이해하는 데 필요한 문서입니다.",
					"ALB target response time과 정상 타깃 분산"
			),
			relation(
					"rds-failure",
					"rds-multi-az",
					"RDS 인프라 장애 시 자동 장애 조치가 필요한지 판단하는 데 필요한 문서입니다.",
					"Multi-AZ 장애 조치와 비용 및 재연결 조건"
			),
			relation(
					"rds-failure",
					"rds-connection-management",
					"RDS 장애 조치 중 애플리케이션 연결이 어떻게 회복되는지 이해하는 데 필요한 문서입니다.",
					"커넥션 풀 회복, 타임아웃과 제한된 재시도"
			),
			relation(
					"rds-failure",
					"read-replica-read-scaling",
					"Read Replica가 읽기 확장과 쓰기 장애 대응에서 가지는 역할 차이를 판단하는 데 필요한 문서입니다.",
					"Read Replica와 Multi-AZ의 목적 차이"
			),
			relation(
					"read-heavy-performance",
					"rds-connection-management",
					"조회 지연의 원인이 연결 수와 쿼리 실행 시간에 있는지 판단하는 데 필요한 문서입니다.",
					"RDS 연결 수, 느린 쿼리와 애플리케이션 대기"
			),
			relation(
					"read-heavy-performance",
					"read-replica-read-scaling",
					"읽기 부하를 복제본으로 분산할 때 최신성 위험을 판단하는 데 필요한 문서입니다.",
					"복제 지연과 읽기/쓰기 분리 복잡도"
			),
			relation(
					"read-heavy-performance",
					"redis-cache",
					"반복 조회를 캐시할 때 성능 개선과 정합성 위험을 판단하는 데 필요한 문서입니다.",
					"TTL, 캐시 무효화와 Redis 장애 시 RDS fallback"
			),
			relation(
					"redis-failure-fallback",
					"redis-cache",
					"Redis 장애 시 cache miss와 fallback이 RDS 부하로 확산되는 흐름을 이해하는 데 필요한 문서입니다.",
					"Redis 장애 우회, TTL 분산, cache stampede 방지"
			),
			relation(
					"redis-failure-fallback",
					"rds-connection-management",
					"캐시 장애 중 RDS 연결과 쿼리 부하를 보호하는 기준을 판단하는 데 필요한 문서입니다.",
					"fallback 요청 제한과 RDS 연결 보호"
			),
			relation(
					"rds-connection-pool-exhaustion",
					"rds-connection-management",
					"Connection Pool 고갈과 RDS 연결 포화를 함께 판단하는 데 필요한 문서입니다.",
					"풀 크기, timeout, 느린 쿼리, RDS 최대 연결 수"
			),
			relation(
					"rds-connection-pool-exhaustion",
					"auto-scaling-basics",
					"EC2 확장이 RDS 연결 수를 함께 늘릴 수 있는 위험을 이해하는 데 필요한 문서입니다.",
					"인스턴스 수 증가와 커넥션 풀 총량의 관계"
			),
			relation(
					"alb-health-check-failure",
					"alb-traffic-distribution",
					"ALB Health Check 실패가 target 제외와 503으로 이어지는 흐름을 이해하는 데 필요한 문서입니다.",
					"Health Check 경로, 임계값, 정상 target 분산"
			),
			relation(
					"alb-health-check-failure",
					"security-group-least-privilege",
					"ALB에서 EC2로 가는 포트 허용이 Health Check에 미치는 영향을 판단하는 데 필요한 문서입니다.",
					"ALB Security Group에서 EC2 Security Group으로 이어지는 허용 규칙"
			),
			relation(
					"private-subnet-nat-missing",
					"nat-gateway-outbound-communication",
					"Private subnet 서버의 외부 통신 실패를 NAT Gateway와 라우팅 관점에서 판단하는 데 필요한 문서입니다.",
					"NAT Gateway 경로, Public subnet 배치, 아웃바운드 비용"
			),
			relation(
					"private-subnet-nat-missing",
					"private-subnet-application-server",
					"Private subnet 서버를 직접 노출하지 않으면서 필요한 아웃바운드만 여는 구조를 이해하는 데 필요한 문서입니다.",
					"Private EC2 인바운드 차단과 아웃바운드 경로 분리"
			),
			relation(
					"security-group-misconfiguration",
					"security-group-least-privilege",
					"Security Group 오설정으로 막힌 요청 경로와 과도한 노출을 함께 판단하는 데 필요한 문서입니다.",
					"ALB, EC2, RDS 간 Security Group 참조와 최소 허용"
			),
			relation(
					"security-group-misconfiguration",
					"alb-traffic-distribution",
					"ALB와 EC2 사이의 요청 전달 실패를 Health Check와 target 흐름으로 확인하는 데 필요한 문서입니다.",
					"ALB target 요청 경로와 Security Group 차단 영향"
			)
	);

	private LearningRelations() {
	}

	public static List<LearningRelation> forScenario(String scenarioKey) {
		return RELATIONS.stream()
				.filter(relation -> relation.scenarioKey().equals(scenarioKey))
				.toList();
	}

	public static List<LearningRelation> all() {
		return RELATIONS;
	}

	public static List<LearningRelation> forDocument(String documentKey) {
		return RELATIONS.stream()
				.filter(relation -> relation.documentKey().equals(documentKey))
				.toList();
	}

	private static LearningRelation relation(String scenarioKey, String documentKey, String learningReason, String reviewFocus) {
		return new LearningRelation(scenarioKey, documentKey, learningReason, reviewFocus);
	}
}
