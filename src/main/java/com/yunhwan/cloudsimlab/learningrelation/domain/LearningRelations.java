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
			)
	);

	private LearningRelations() {
	}

	public static List<LearningRelation> forScenario(String scenarioKey) {
		return RELATIONS.stream()
				.filter(relation -> relation.scenarioKey().equals(scenarioKey))
				.toList();
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
