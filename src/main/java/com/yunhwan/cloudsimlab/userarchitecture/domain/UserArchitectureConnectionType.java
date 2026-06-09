package com.yunhwan.cloudsimlab.userarchitecture.domain;

public enum UserArchitectureConnectionType {
	REQUEST_FLOW(
			"Request Flow",
			"사용자 요청이나 애플리케이션 호출이 source에서 target으로 전달되는 흐름입니다."
	),
	NETWORK_ROUTE(
			"Network Route",
			"서브넷, 게이트웨이, 외부 네트워크 사이에 트래픽이 이동할 수 있는 라우팅 경로입니다."
	),
	DEPENDS_ON(
			"Depends On",
			"source 리소스가 동작하거나 복구되기 위해 target 리소스에 의존하는 관계입니다."
	),
	REPLICATION(
			"Replication",
			"데이터가 primary 리소스에서 standby 또는 replica 리소스로 복제되는 관계입니다."
	),
	SECURITY_RULE(
			"Security Rule",
			"Security Group 같은 보안 경계가 특정 source와 target 사이의 접근을 허용하는 관계입니다."
	);

	private final String displayName;
	private final String meaning;

	UserArchitectureConnectionType(String displayName, String meaning) {
		this.displayName = displayName;
		this.meaning = meaning;
	}

	public String getKey() {
		return name();
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMeaning() {
		return meaning;
	}
}
