package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioSeedPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

@Configuration
@Profile("local")
class ScenarioSeed {

	@Bean
	CommandLineRunner seedScenarios(ScenarioSeedPort seedPort) {
		return args -> {
			if (seedPort.count() > 0) {
				return;
			}

			seedPort.save(Scenario.newScenario(
					"단일 Spring Boot 배포",
					ScenarioCategory.COMPUTE,
					ScenarioLevel.BEGINNER,
					"단일 EC2 장애 지점과 배포 중단 위험을 줄이는 기본 운영 구조를 선택합니다.",
					"현재 Spring Boot API가 EC2 한 대에서 실행되고 있고 Client 요청이 이 서버로 직접 들어옵니다. 배포 중 재시작이 발생하면 전체 요청이 실패하고, 인스턴스 장애가 나면 RDS는 정상이어도 서비스가 중단됩니다. 비용을 크게 늘리지 않으면서 장애 우회와 점진적 확장이 가능한 구조를 선택해야 합니다.",
					List.of("Client", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("EC2 한 대 유지", "비용과 운영 복잡도는 가장 낮지만 배포, 장애, 트래픽 증가가 모두 단일 서버에 집중됩니다.", 1, false, 1),
							ScenarioOption.newOption("ALB와 Auto Scaling 추가", "비용과 설정 복잡도는 늘지만 정상 인스턴스로 요청을 분산하고 장애 인스턴스를 우회할 수 있습니다.", 3, true, 0),
							ScenarioOption.newOption("EC2 사양만 크게 변경", "CPU와 메모리 여유는 생기지만 단일 장애 지점과 배포 중단 문제는 해결하지 못합니다.", 2, false, 0)
					)
			));
			seedPort.save(Scenario.newScenario(
					"Private subnet 애플리케이션 서버",
					ScenarioCategory.NETWORK,
					ScenarioLevel.BEGINNER,
					"외부 진입점은 유지하면서 애플리케이션 서버의 직접 노출을 줄입니다.",
					"EC2 애플리케이션 서버가 Public subnet에 있고 Security Group 실수 시 서버 포트가 인터넷에 직접 노출될 수 있습니다. Client 요청은 계속 받아야 하지만 EC2와 RDS는 외부에서 직접 접근하지 못하게 분리해야 합니다. 동시에 애플리케이션 서버가 외부 API 호출이나 패키지 업데이트를 수행할 수 있는지도 고려해야 합니다.",
					List.of("Client", "Internet Gateway", "Public subnet", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("EC2를 Public subnet에 유지", "구성은 단순하지만 관리 포트나 애플리케이션 포트가 잘못 열리면 보안 위험이 커집니다.", 1, false, 2),
							ScenarioOption.newOption("ALB 앞단과 Private subnet EC2로 분리", "Client 진입점은 ALB로 유지하고 EC2 인바운드는 ALB에서 오는 요청으로 제한할 수 있습니다.", 3, true, 0),
							ScenarioOption.newOption("Private subnet EC2와 NAT Gateway 구성", "서버 직접 노출은 줄이고 아웃바운드 통신은 확보하지만 NAT Gateway 비용과 라우팅 복잡도가 추가됩니다.", 2, false, 0)
					)
			));
			seedPort.save(Scenario.newScenario(
					"트래픽 급증 대응",
					ScenarioCategory.COMPUTE,
					ScenarioLevel.INTERMEDIATE,
					"EC2 계층의 요청 증가를 흡수하면서 비용과 확장 지연을 함께 판단합니다.",
					"이벤트 시작 후 Client 요청이 평소보다 크게 늘었고 ALB target response time과 EC2 CPU 사용률이 동시에 상승했습니다. RDS CPU는 아직 여유가 있지만 API 응답 시간이 길어지고 있습니다. 현재 병목이 애플리케이션 서버 계층에 있는지 판단하고, 피크를 따라갈 수 있는 확장 전략을 선택해야 합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Auto Scaling 추가", "요청 증가에 맞춰 EC2 수를 늘려 처리량과 가용성을 높이지만 시작 시간과 추가 비용을 고려해야 합니다.", 3, true, 0),
							ScenarioOption.newOption("EC2 사양만 크게 변경", "즉시 CPU 여유를 만들 수 있지만 피크가 더 커지면 다시 한계가 오고 장애 우회 능력도 제한적입니다.", 2, false, 0),
							ScenarioOption.newOption("RDS만 증설", "현재 병목이 DB가 아니라면 비용만 늘고 API 서버의 CPU와 응답 시간 문제는 남습니다.", 0, false, 1)
					)
			));
			seedPort.save(Scenario.newScenario(
					"RDS 장애 대응",
					ScenarioCategory.STORAGE,
					ScenarioLevel.INTERMEDIATE,
					"RDS 단일 장애가 서비스 전체 중단으로 이어지는 시간을 줄입니다.",
					"운영 중인 RDS 인스턴스에 장애가 발생하면 EC2와 ALB는 정상이어도 쓰기와 조회가 모두 실패합니다. 최근 장애 대응 목표는 데이터 유실보다 서비스 중단 시간을 줄이는 쪽에 가깝습니다. 장애 조치 자동화, 복구 시간, 비용 증가, 애플리케이션 재연결 흐름을 함께 고려해야 합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Multi-AZ 활성화", "비용은 증가하지만 자동 장애 조치로 RDS 인스턴스 장애 시 복구 시간을 줄일 수 있습니다.", 3, true, 0),
							ScenarioOption.newOption("Read Replica만 추가", "읽기 부하 분산에는 도움이 되지만 쓰기 DB 장애에 대한 자동 장애 조치 대책으로는 부족합니다.", 1, false, 1),
							ScenarioOption.newOption("백업 주기만 늘리기", "데이터 복구 가능성은 높이지만 장애가 발생한 순간의 서비스 중단 시간은 줄이지 못합니다.", 0, false, 1)
					)
			));
			seedPort.save(Scenario.newScenario(
					"조회 중심 성능 문제",
					ScenarioCategory.STORAGE,
					ScenarioLevel.INTERMEDIATE,
					"읽기 중심 트래픽에서 RDS 부하를 줄이면서 일관성 위험을 판단합니다.",
					"상품 목록과 상세 조회가 급증하면서 RDS CPU와 쿼리 시간이 상승했습니다. 쓰기 요청은 많지 않고 대부분 반복 조회지만, 일부 화면은 방금 변경한 상품 정보가 바로 보여야 합니다. 조회 경로를 캐시하거나 읽기 복제본으로 분리할 때 성능, 비용, 복잡도, 데이터 최신성 trade-off를 함께 판단해야 합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Redis Cache 추가", "반복 조회 응답 시간을 줄이고 RDS 부하를 낮추지만 TTL, 무효화, 장애 시 우회 전략이 필요합니다.", 2, true, 0),
							ScenarioOption.newOption("Read Replica 추가", "읽기 부하를 분산할 수 있지만 복제 지연과 읽기/쓰기 분리 복잡도를 감수해야 합니다.", 2, true, 0),
							ScenarioOption.newOption("EC2만 증설", "애플리케이션 처리 여유는 늘지만 RDS CPU와 쿼리 시간 병목은 그대로 남을 수 있습니다.", 1, false, 1)
					)
			));
		};
	}
}
