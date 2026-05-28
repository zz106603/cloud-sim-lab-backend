package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@Configuration
@Profile("local")
class LearningDocumentSeed {

	@Bean
	CommandLineRunner seedLearningDocuments(LearningDocumentSeedPort seedPort) {
		return args -> {
			if (seedPort.count() > 0) {
				return;
			}

			seedPort.save(LearningDocument.newDocument(
					"EC2와 컴퓨팅 용량",
					DocumentCategory.COMPUTE,
					DocumentLevel.BEGINNER,
					"Spring Boot 같은 애플리케이션을 EC2에서 실행할 때 용량 선택이 왜 중요한지 이해합니다.",
					"EC2는 애플리케이션을 실행하는 기본 컴퓨팅 자원입니다. 요청은 Client에서 들어와 EC2의 애플리케이션으로 전달되고, 필요하면 RDS 같은 저장소를 호출합니다. 용량이 작으면 CPU나 메모리가 먼저 병목이 되고, 용량이 크면 비용이 빠르게 증가합니다. 장애가 나면 단일 EC2 구성은 서비스 전체 중단으로 이어질 수 있으므로 트래픽, 비용, 장애 허용 범위를 함께 봐야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Private subnet과 애플리케이션 서버",
					DocumentCategory.NETWORK,
					DocumentLevel.BEGINNER,
					"애플리케이션 서버를 인터넷에 직접 노출하지 않는 이유를 이해합니다.",
					"Private subnet은 외부에서 바로 접근하면 안 되는 EC2나 RDS를 배치하는 영역입니다. 일반적인 요청 흐름은 Client가 ALB에 접근하고, ALB가 Private subnet의 EC2로 트래픽을 전달하는 방식입니다. EC2가 외부 API나 패키지 저장소에 나가야 하면 NAT Gateway가 필요할 수 있습니다. 잘못 구성하면 보안 노출이 커지거나, 반대로 필요한 아웃바운드 통신이 막힐 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"ALB와 트래픽 분산",
					DocumentCategory.NETWORK,
					DocumentLevel.BEGINNER,
					"여러 EC2로 요청을 나누고 장애 인스턴스를 우회하는 기본 흐름을 이해합니다.",
					"ALB는 Client 요청을 받아 정상 상태의 EC2로 분산합니다. 단일 EC2에 직접 연결하는 구조보다 배포와 장애 대응이 쉬워집니다. 다만 ALB 자체 비용이 추가되고, Health Check 경로가 잘못되면 정상 서버도 제외될 수 있습니다. Security Group은 Client가 ALB에 접근하고 ALB만 EC2에 접근하도록 좁게 여는 것이 기본입니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Auto Scaling 기본",
					DocumentCategory.COMPUTE,
					DocumentLevel.INTERMEDIATE,
					"트래픽 변화에 맞춰 EC2 수를 자동으로 조절하는 이유와 주의점을 이해합니다.",
					"Auto Scaling은 CPU, 요청 수 같은 지표에 따라 EC2 수를 늘리거나 줄입니다. 트래픽 급증 시 성능과 가용성을 높일 수 있지만, 새 인스턴스가 준비되는 시간과 배포 이미지 품질이 중요합니다. 설정이 과하면 비용이 증가하고, 너무 보수적이면 피크를 따라가지 못합니다. 상태를 EC2 로컬에 저장하는 애플리케이션은 확장 시 일관성 문제가 생길 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Security Group 최소 허용",
					DocumentCategory.SECURITY,
					DocumentLevel.BEGINNER,
					"필요한 포트와 출발지만 열어 공격 표면을 줄이는 방법을 이해합니다.",
					"Security Group은 EC2, ALB, RDS 같은 리소스의 인바운드/아웃바운드 트래픽을 제어합니다. 운영에서는 Client가 ALB로, ALB가 EC2로, EC2가 RDS로 접근하는 흐름만 허용하는 식으로 좁히는 것이 안전합니다. 0.0.0.0/0을 관리 포트에 열면 편하지만 보안 위험이 큽니다. 규칙을 너무 강하게 막으면 배포, 모니터링, 외부 API 호출이 실패할 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"RDS와 연결 관리",
					DocumentCategory.STORAGE,
					DocumentLevel.BEGINNER,
					"애플리케이션이 RDS를 사용할 때 연결 수와 장애 영향을 이해합니다.",
					"RDS는 애플리케이션 데이터의 중심 저장소입니다. EC2의 Spring Boot 애플리케이션은 커넥션 풀을 통해 RDS에 쿼리를 보냅니다. 트래픽이 늘면 CPU뿐 아니라 DB 연결 수와 쿼리 시간이 병목이 될 수 있습니다. 연결 풀을 과하게 키우면 RDS가 더 빨리 포화되고, 너무 작으면 애플리케이션 대기 시간이 길어집니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"RDS Multi-AZ",
					DocumentCategory.STORAGE,
					DocumentLevel.INTERMEDIATE,
					"RDS 장애 시 자동 장애 조치가 필요한 이유를 이해합니다.",
					"Multi-AZ는 RDS를 다른 가용 영역에 대기 복제본과 함께 운영해 장애 시 자동으로 전환할 수 있게 합니다. 애플리케이션은 같은 엔드포인트를 사용하지만 전환 중에는 짧은 연결 끊김이 발생할 수 있습니다. 가용성은 좋아지지만 비용이 늘고, 장애 조치 시간을 완전히 0으로 만들지는 못합니다. 애플리케이션의 재시도와 타임아웃 설정도 함께 점검해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Read Replica와 읽기 확장",
					DocumentCategory.STORAGE,
					DocumentLevel.INTERMEDIATE,
					"조회 트래픽이 많을 때 읽기 부하를 분리하는 방법과 한계를 이해합니다.",
					"Read Replica는 RDS의 읽기 전용 복제본입니다. 목록 조회나 리포트처럼 최신성이 조금 늦어도 되는 요청을 분산하면 원본 RDS의 부하를 줄일 수 있습니다. 쓰기는 여전히 원본으로 가야 하며, 복제 지연 때문에 방금 쓴 데이터가 바로 보이지 않을 수 있습니다. 애플리케이션에서 읽기/쓰기 경로를 나누는 복잡도도 생깁니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Redis Cache",
					DocumentCategory.COMPUTE,
					DocumentLevel.INTERMEDIATE,
					"반복 조회를 빠르게 처리하기 위해 Redis를 사용하는 이유와 주의점을 이해합니다.",
					"Redis는 자주 읽는 데이터를 메모리에 저장해 RDS 조회를 줄이는 데 유용합니다. Client 요청이 EC2에 도착하면 애플리케이션은 먼저 Redis를 확인하고, 없으면 RDS에서 읽은 뒤 Redis에 저장할 수 있습니다. 성능은 좋아지지만 캐시 만료, 무효화, 데이터 일관성 문제가 생깁니다. 장애 시 RDS로 부하가 몰릴 수 있으므로 TTL과 우회 흐름을 준비해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"NAT Gateway와 아웃바운드 통신",
					DocumentCategory.NETWORK,
					DocumentLevel.INTERMEDIATE,
					"Private subnet의 EC2가 안전하게 외부로 나가는 흐름을 이해합니다.",
					"NAT Gateway는 Private subnet의 EC2가 인터넷으로 나가는 아웃바운드 통신을 할 수 있게 합니다. 외부에서 EC2로 직접 들어오는 경로를 만들지는 않습니다. 패키지 다운로드, 외부 API 호출, 보안 업데이트에 필요할 수 있지만 시간당 비용과 데이터 처리 비용이 있습니다. NAT Gateway 장애나 라우팅 실수는 배포 실패와 외부 연동 장애로 이어질 수 있습니다."
			));
		};
	}
}
