package com.yunhwan.cloudsimlab.userarchitecture.domain;

public enum UserArchitectureResourceType {
	CLIENT(
			"Client",
			"사용자 요청이 시작되는 외부 클라이언트입니다.",
			"ACTOR",
			"요청 흐름의 시작점과 외부 노출 경계를 표시할 때 사용합니다."
	),
	VPC(
			"VPC",
			"AWS 리소스를 배치하는 논리적 네트워크 경계입니다.",
			"NETWORK",
			"서브넷, 게이트웨이, 보안 경계를 하나의 네트워크 범위로 묶을 때 사용합니다."
	),
	SUBNET(
			"Subnet",
			"리소스의 네트워크 노출 범위와 라우팅 경계를 나누는 영역입니다.",
			"NETWORK",
			"Public subnet과 Private subnet 배치 판단을 학습할 때 사용합니다."
	),
	EC2(
			"EC2",
			"애플리케이션 요청을 처리하는 컴퓨팅 리소스입니다.",
			"COMPUTE",
			"서버 용량, 장애 지점, 배포 영향 범위를 표현할 때 사용합니다."
	),
	ALB(
			"ALB",
			"HTTP 요청을 정상 target으로 분산하고 장애 인스턴스를 우회하는 진입점입니다.",
			"TRAFFIC",
			"외부 진입점, target 분산, health check 흐름을 표현할 때 사용합니다."
	),
	TARGET_GROUP(
			"Target Group",
			"ALB가 트래픽을 전달하고 health check 상태를 판단하는 대상 그룹입니다.",
			"TRAFFIC",
			"ALB와 EC2 사이의 정상 target 판정 범위를 표시할 때 사용합니다."
	),
	AUTO_SCALING_GROUP(
			"Auto Scaling Group",
			"부하나 장애 상황에 맞춰 애플리케이션 인스턴스 수를 조정합니다.",
			"COMPUTE",
			"scale-out과 장애 인스턴스 대체 흐름을 표현할 때 사용합니다."
	),
	RDS(
			"RDS",
			"애플리케이션의 주요 영속 데이터를 저장하는 관계형 데이터베이스입니다.",
			"DATA",
			"쓰기 기준 DB, 장애 지점, 연결 병목을 표현할 때 사용합니다."
	),
	RDS_STANDBY(
			"RDS Standby",
			"Multi-AZ 구성에서 장애 조치 대상이 되는 대기 데이터베이스입니다.",
			"DATA",
			"RDS 장애 대응과 자동 failover 학습 흐름을 표현할 때 사용합니다."
	),
	READ_REPLICA(
			"Read Replica",
			"읽기 요청을 분산하지만 복제 지연을 고려해야 하는 RDS 복제본입니다.",
			"DATA",
			"조회 성능 개선과 최신성 trade-off를 표현할 때 사용합니다."
	),
	REDIS(
			"Redis",
			"반복 조회를 빠르게 처리하고 DB 부하를 줄이는 캐시 계층입니다.",
			"CACHE",
			"캐시 hit, fallback, 장애 전파 위험을 표현할 때 사용합니다."
	),
	CONNECTION_POOL(
			"Connection Pool",
			"애플리케이션과 DB 사이의 동시 연결 수와 대기 흐름을 제한하는 계층입니다.",
			"APPLICATION",
			"DB 연결 고갈과 풀 크기 조정 판단을 학습할 때 사용합니다."
	),
	HEALTH_CHECK(
			"Health Check",
			"트래픽을 받을 수 있는 인스턴스인지 주기적으로 확인하는 검사입니다.",
			"OPERATIONS",
			"ALB 503, readiness 경로, target 제외 원인을 표현할 때 사용합니다."
	),
	NAT_GATEWAY(
			"NAT Gateway",
			"Private subnet 리소스의 인터넷 아웃바운드 통신 경로를 제공합니다.",
			"NETWORK",
			"서버 직접 노출 없이 외부 API나 패키지 저장소에 접근하는 흐름을 표시할 때 사용합니다."
	),
	INTERNET_GATEWAY(
			"Internet Gateway",
			"VPC와 인터넷 사이의 인바운드/아웃바운드 통신 경로를 제공합니다.",
			"NETWORK",
			"Public subnet과 외부 인터넷 연결 경계를 표시할 때 사용합니다."
	),
	SECURITY_GROUP(
			"Security Group",
			"리소스 인바운드와 아웃바운드 접근을 제한하는 보안 경계입니다.",
			"SECURITY",
			"ALB, EC2, RDS 사이의 최소 허용 규칙을 표현할 때 사용합니다."
	),
	EXTERNAL_SERVICE(
			"External Service",
			"결제사 API처럼 VPC 밖에 있는 외부 의존 서비스입니다.",
			"EXTERNAL",
			"Private subnet 아웃바운드 장애와 외부 의존성 호출을 표현할 때 사용합니다."
	);

	private final String displayName;
	private final String description;
	private final String visualizationCategory;
	private final String learningPurpose;

	UserArchitectureResourceType(
			String displayName,
			String description,
			String visualizationCategory,
			String learningPurpose
	) {
		this.displayName = displayName;
		this.description = description;
		this.visualizationCategory = visualizationCategory;
		this.learningPurpose = learningPurpose;
	}

	public String getKey() {
		return name();
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public String getVisualizationCategory() {
		return visualizationCategory;
	}

	public String getLearningPurpose() {
		return learningPurpose;
	}
}
