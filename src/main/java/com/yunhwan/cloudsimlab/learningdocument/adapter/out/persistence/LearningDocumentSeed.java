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
					"EC2 용량 선택이 성능, 비용, 장애 영향에 어떤 판단 기준을 만드는지 이해합니다.",
					"EC2는 Spring Boot 같은 애플리케이션이 실제 요청을 처리하는 컴퓨팅 자원입니다. Client 요청은 EC2의 애플리케이션으로 들어오고, 애플리케이션은 필요한 데이터를 RDS나 Redis 같은 의존 리소스에서 조회합니다. CPU 사용률, 메모리 사용량, 응답 시간, 스레드 대기 시간이 함께 높아지면 EC2의 컴퓨팅 자원(용량)이 병목 지점일 가능성이 큽니다. 이때 인스턴스 사양을 키우면 빠르게 완화할 수 있지만 비용이 증가하고 단일 장애 지점은 그대로 남습니다. 트래픽 변동이 크거나 배포 중단까지 줄여야 한다면 ALB와 Auto Scaling을 함께 검토해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Private subnet과 애플리케이션 서버",
					DocumentCategory.NETWORK,
					DocumentLevel.BEGINNER,
					"애플리케이션 서버를 인터넷에 직접 노출하지 않는 네트워크 판단 기준을 이해합니다.",
					"Private subnet은 외부 사용자가 직접 접근하면 안 되는 EC2나 RDS를 배치하는 네트워크 영역입니다. 운영 요청 흐름은 Client가 Public subnet의 ALB에 접근하고, ALB가 Private subnet의 EC2로 트래픽을 전달하는 방식이 기본입니다. 이렇게 구성하면 애플리케이션 서버의 인바운드 경로를 ALB로 제한할 수 있어 보안 위험을 줄입니다. 다만 Private subnet의 EC2가 외부 API 호출, 패키지 다운로드, 보안 업데이트를 수행해야 한다면 NAT Gateway나 별도 아웃바운드 경로가 필요합니다. 서브넷 라우팅 테이블이나 Security Group 설정을 잘못하면 필요한 통신이 막혀 배포와 외부 연동이 실패할 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"ALB와 트래픽 분산",
					DocumentCategory.NETWORK,
					DocumentLevel.BEGINNER,
					"ALB가 트래픽 분산, 배포 안정성, 장애 우회에 주는 효과를 이해합니다.",
					"ALB는 Client 요청을 받아 정상 상태인 EC2 인스턴스로 분산하는 진입점입니다. 단일 EC2에 직접 연결하면 배포나 장애 시 요청을 우회하기 어렵지만, ALB를 두면 여러 EC2로 요청을 나누고 비정상 인스턴스를 대상 그룹에서 제외할 수 있습니다. 판단할 때는 요청 수, 5xx 비율, target response time, Health Check 실패 여부를 함께 봐야 합니다. ALB는 가용성과 운영 편의성을 높이지만 비용과 설정 복잡도를 추가합니다. Health Check 경로가 실제 애플리케이션 상태를 제대로 반영하지 못하면 정상 서버가 제외되거나 장애 서버가 계속 트래픽을 받을 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Auto Scaling 기본",
					DocumentCategory.COMPUTE,
					DocumentLevel.INTERMEDIATE,
					"트래픽 변화에 맞춰 EC2 수를 조절할 때 필요한 지표와 한계를 이해합니다.",
					"Auto Scaling은 CPU 사용률, ALB 요청 수, 타깃 응답 시간 같은 지표에 따라 EC2 수를 늘리거나 줄이는 운영 장치입니다. 갑작스러운 요청 증가로 EC2 CPU와 응답 시간이 함께 상승한다면 수평 확장이 효과적일 수 있습니다. 하지만 새 인스턴스가 시작되고 Health Check를 통과하기까지 시간이 걸리므로 피크가 시작된 뒤에만 대응하면 늦을 수 있습니다. 최소/최대 인스턴스 수와 스케일링 정책을 공격적으로 잡으면 비용이 빠르게 늘고, 보수적으로 잡으면 장애를 막지 못합니다. 세션, 업로드 파일, 임시 상태를 EC2 로컬에 저장하는 구조라면 확장 후 요청 분산 과정에서 일관성 문제가 생길 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Security Group 최소 허용",
					DocumentCategory.SECURITY,
					DocumentLevel.BEGINNER,
					"필요한 포트와 출발지만 허용해 공격 표면을 줄이는 기준을 이해합니다.",
					"Security Group은 ALB, EC2, RDS 같은 리소스의 네트워크 접근을 제어하는 방화벽 역할을 합니다. 기본 판단은 실제 요청 흐름만 허용하는 것입니다. Client는 ALB의 HTTP/HTTPS 포트로, ALB는 EC2의 애플리케이션 포트로, EC2는 RDS의 DB 포트로 접근하도록 제한합니다. 관리 포트나 DB 포트를 0.0.0.0/0에 열면 운영은 편해 보이지만 공격 표면이 크게 증가합니다. 반대로 필요한 아웃바운드나 내부 통신까지 막으면 배포, 모니터링, 외부 API 호출, DB 연결이 실패할 수 있으므로 보안과 운영 흐름을 함께 검증해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"RDS와 연결 관리",
					DocumentCategory.STORAGE,
					DocumentLevel.BEGINNER,
					"RDS 병목을 판단할 때 연결 수, 쿼리 시간, 커넥션 풀 설정을 함께 이해합니다.",
					"RDS는 애플리케이션 데이터의 중심 저장소이고, Spring Boot 애플리케이션은 보통 커넥션 풀을 통해 RDS에 쿼리를 보냅니다. 트래픽이 늘면 EC2 CPU뿐 아니라 DB CPU, active connection 수, slow query, lock wait가 함께 병목이 될 수 있습니다. 커넥션 풀을 크게 늘리면 애플리케이션 대기는 줄어 보일 수 있지만 RDS가 더 빨리 포화될 수 있습니다. 너무 작게 잡으면 DB는 여유가 있어도 애플리케이션 스레드가 연결을 기다리며 응답 시간이 늘어납니다. RDS 장애나 재시작이 발생하면 기존 연결이 끊기므로 타임아웃, 재시도, 장애 전환 시간을 함께 고려해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"RDS Multi-AZ",
					DocumentCategory.STORAGE,
					DocumentLevel.INTERMEDIATE,
					"RDS 장애 시 자동 장애 조치가 필요한 상황과 비용 trade-off를 이해합니다.",
					"RDS Multi-AZ는 운영 DB를 다른 가용 영역의 대기 인스턴스와 함께 구성해 장애 시 자동으로 전환할 수 있게 합니다. RDS 인스턴스 장애, 스토리지 장애, 가용 영역 장애가 서비스 전체 중단으로 이어지는 것이 가장 큰 위험일 때 우선 검토합니다. 애플리케이션은 같은 엔드포인트를 사용하지만 장애 조치 중에는 짧은 연결 끊김과 재연결 지연이 발생할 수 있습니다. Multi-AZ는 가용성을 높이지만 읽기 성능 확장 기능은 아니며 비용도 증가합니다. 따라서 재시도, 커넥션 풀 회복, 타임아웃 설정까지 함께 준비해야 실제 장애 시간을 줄일 수 있습니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Read Replica와 읽기 확장",
					DocumentCategory.STORAGE,
					DocumentLevel.INTERMEDIATE,
					"조회 트래픽을 분산할 때 Read Replica가 해결하는 문제와 한계를 이해합니다.",
					"Read Replica는 RDS의 읽기 전용 복제본으로, 조회 요청을 원본 DB에서 분리해 부하를 낮추는 선택지입니다. 목록 조회, 검색 보조 조회, 리포트처럼 최신성이 약간 늦어도 되는 요청에 특히 적합합니다. 판단할 때는 읽기 비율, 원본 RDS CPU, replica lag, 최신 데이터 요구 수준을 함께 봐야 합니다. 쓰기는 여전히 원본 RDS로 가야 하며, 방금 저장한 데이터를 즉시 읽어야 하는 기능은 복제 지연으로 혼란이 생길 수 있습니다. 애플리케이션에서 읽기/쓰기 경로를 나누는 복잡도와 장애 시 fallback 흐름도 함께 설계해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"Redis Cache",
					DocumentCategory.COMPUTE,
					DocumentLevel.INTERMEDIATE,
					"반복 조회를 캐시할 때 성능 개선과 데이터 일관성 위험을 함께 이해합니다.",
					"Redis는 자주 읽는 데이터를 메모리에 저장해 RDS 조회 횟수와 응답 시간을 줄이는 캐시 계층입니다. 일반적인 cache-aside 흐름에서는 EC2 애플리케이션이 먼저 Redis를 조회하고, 없으면 RDS에서 읽은 뒤 Redis에 저장합니다. 상품 목록, 설정값, 인기 콘텐츠처럼 반복 조회가 많고 즉시 최신성이 덜 중요한 데이터에 효과적입니다. 하지만 TTL, 캐시 무효화, 동시 갱신, 장애 시 우회 전략이 없으면 오래된 데이터를 보여주거나 Redis 장애가 RDS 부하 폭증으로 이어질 수 있습니다. 캐시는 DB를 대체하는 저장소가 아니라 조회 부하를 완화하는 운영 선택지로 판단해야 합니다."
			));
			seedPort.save(LearningDocument.newDocument(
					"NAT Gateway와 아웃바운드 통신",
					DocumentCategory.NETWORK,
					DocumentLevel.INTERMEDIATE,
					"Private subnet 서버가 외부로 나가야 할 때 NAT Gateway의 역할과 비용을 이해합니다.",
					"NAT Gateway는 Private subnet의 EC2가 인터넷으로 아웃바운드 요청을 보낼 수 있게 하는 관리형 네트워크 리소스입니다. 외부 사용자가 EC2로 직접 들어오는 인바운드 경로를 만들지는 않기 때문에 애플리케이션 서버를 숨긴 상태로 패키지 다운로드, 외부 API 호출, 보안 업데이트를 수행할 수 있습니다. 판단할 때는 서버가 실제로 외부 호출을 해야 하는지, 트래픽 양이 얼마나 되는지, 가용 영역별 NAT 구성이 필요한지 확인해야 합니다. NAT Gateway는 시간당 비용과 데이터 처리 비용이 있고, 라우팅 실수나 단일 NAT 장애는 배포 실패와 외부 연동 장애로 이어질 수 있습니다. 보안을 위해 Private subnet에 서버를 두었다면 아웃바운드 운영 경로까지 함께 설계해야 합니다."
			));
		};
	}
}
