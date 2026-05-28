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
					"웹 서비스 확장",
					ScenarioCategory.COMPUTE,
					ScenarioLevel.BEGINNER,
					"트래픽 증가에 맞춰 컴퓨팅 용량을 선택합니다.",
					"이벤트를 앞두고 웹 서비스 트래픽이 늘어날 예정입니다. 현재 EC2 용량으로 충분한지 판단해야 합니다.",
					List.of("Client", "Load Balancer", "EC2", "RDS"),
					List.of(
							ScenarioOption.newOption("작은 EC2 인스턴스 유지", "비용은 낮지만 CPU와 메모리 여유가 제한적입니다.", 1, false, 0),
							ScenarioOption.newOption("큰 EC2 인스턴스로 변경", "처리 용량은 늘어나지만 시간당 비용도 증가합니다.", 2, true, 0)
					)
			));
			seedPort.save(Scenario.newScenario(
					"퍼블릭/프라이빗 트래픽 분리",
					ScenarioCategory.NETWORK,
					ScenarioLevel.BEGINNER,
					"외부 공개 영역과 내부 처리 영역을 분리합니다.",
					"간단한 서비스에서 인터넷에 직접 노출할 리소스와 내부에 둘 리소스를 구분해야 합니다.",
					List.of("Client", "Internet Gateway", "Public subnet", "Application server"),
					List.of(
							ScenarioOption.newOption("Public subnet에 배치", "리소스가 인터넷 인바운드 트래픽에 직접 노출됩니다.", 1, false, 2),
							ScenarioOption.newOption("Private subnet에 배치", "내부 라우팅을 통해서만 리소스에 접근할 수 있습니다.", 2, true, 0)
					)
			));
		};
	}
}
