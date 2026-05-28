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
					"단일 EC2에서 실행 중인 Spring Boot 서비스를 안정적으로 운영합니다.",
					"현재 Spring Boot API가 EC2 한 대에서 실행되고 있습니다. 배포나 장애 때 전체 서비스가 멈출 수 있어 기본 운영 구조를 개선해야 합니다.",
					List.of("Client", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("EC2 한 대 유지", "비용과 구조는 단순하지만 장애나 배포 중단에 취약합니다.", 1, false, 1),
							ScenarioOption.newOption("ALB와 Auto Scaling 추가", "비용과 설정 복잡도는 늘지만 트래픽 분산과 장애 우회가 가능해집니다.", 3, true, 0),
							ScenarioOption.newOption("EC2 사양만 크게 변경", "일시적인 성능 여유는 생기지만 단일 장애 지점은 그대로 남습니다.", 2, false, 0)
					)
			));
			seedPort.save(Scenario.newScenario(
					"Private subnet 애플리케이션 서버",
					ScenarioCategory.NETWORK,
					ScenarioLevel.BEGINNER,
					"애플리케이션 서버를 인터넷에 직접 노출하지 않도록 분리합니다.",
					"EC2 애플리케이션 서버가 Public subnet에 있어 외부 접근 면적이 큽니다. Client 요청은 받아야 하지만 서버와 RDS는 더 안전하게 보호해야 합니다.",
					List.of("Client", "Internet Gateway", "Public subnet", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("EC2를 Public subnet에 유지", "구성은 쉽지만 Security Group 실수 시 서버가 직접 노출될 위험이 큽니다.", 1, false, 2),
							ScenarioOption.newOption("ALB 앞단과 Private subnet EC2로 분리", "외부 진입점은 ALB로 제한하고 애플리케이션 서버 노출을 줄입니다.", 3, true, 0),
							ScenarioOption.newOption("Private subnet EC2와 NAT Gateway 구성", "보안을 유지하면서 외부 API 호출과 패키지 업데이트 경로를 확보합니다.", 2, false, 0)
					)
			));
			seedPort.save(Scenario.newScenario(
					"트래픽 급증 대응",
					ScenarioCategory.COMPUTE,
					ScenarioLevel.INTERMEDIATE,
					"갑작스러운 요청 증가에도 API 응답을 유지합니다.",
					"이벤트 시작 후 Client 요청이 평소보다 크게 늘었습니다. EC2 CPU가 높고 응답 시간이 길어져 확장 전략을 선택해야 합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Auto Scaling 추가", "트래픽 증가에 맞춰 EC2 수를 늘릴 수 있지만 시작 시간과 비용 증가를 고려해야 합니다.", 3, true, 0),
							ScenarioOption.newOption("EC2 사양만 크게 변경", "빠른 완화는 가능하지만 피크가 더 커지면 다시 한계가 오고 가용성도 크게 개선되지 않습니다.", 2, false, 0),
							ScenarioOption.newOption("RDS만 증설", "DB 병목이 아닌 상황에서는 비용만 늘고 API 서버 병목은 남을 수 있습니다.", 0, false, 1)
					)
			));
			seedPort.save(Scenario.newScenario(
					"RDS 장애 대응",
					ScenarioCategory.STORAGE,
					ScenarioLevel.INTERMEDIATE,
					"RDS 장애가 서비스 전체 중단으로 이어지지 않게 합니다.",
					"운영 중인 RDS 인스턴스에 장애가 발생하면 쓰기와 조회가 모두 중단됩니다. 장애 전환과 복구 시간을 줄이는 구성이 필요합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Multi-AZ 활성화", "비용은 증가하지만 RDS 장애 시 자동 장애 조치로 복구 시간을 줄일 수 있습니다.", 3, true, 0),
							ScenarioOption.newOption("Read Replica만 추가", "읽기 확장에는 도움이 되지만 기본 장애 조치 대책으로는 부족할 수 있습니다.", 1, false, 1),
							ScenarioOption.newOption("백업 주기만 늘리기", "데이터 복구에는 도움이 되지만 장애 순간의 서비스 중단은 막지 못합니다.", 0, false, 1)
					)
			));
			seedPort.save(Scenario.newScenario(
					"조회 중심 성능 문제",
					ScenarioCategory.STORAGE,
					ScenarioLevel.INTERMEDIATE,
					"읽기 트래픽이 많은 API의 RDS 부하를 낮춥니다.",
					"상품 목록과 상세 조회가 급증하면서 RDS CPU와 쿼리 시간이 상승했습니다. 쓰기보다 읽기 요청이 많아 조회 경로를 분리하거나 캐시해야 합니다.",
					List.of("Client", "ALB", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("Redis Cache 추가", "반복 조회를 빠르게 처리하고 RDS 부하를 줄이지만 캐시 무효화와 일관성 관리가 필요합니다.", 2, true, 0),
							ScenarioOption.newOption("Read Replica 추가", "읽기 부하를 분산할 수 있지만 복제 지연과 읽기/쓰기 분리 복잡도가 생깁니다.", 2, true, 0),
							ScenarioOption.newOption("EC2만 증설", "애플리케이션 처리 여유는 늘지만 RDS 조회 병목은 그대로 남을 수 있습니다.", 1, false, 1)
					)
			));
		};
	}
}
