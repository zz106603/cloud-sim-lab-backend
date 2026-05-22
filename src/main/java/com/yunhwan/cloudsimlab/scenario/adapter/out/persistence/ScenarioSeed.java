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
					"Scale a web service",
					ScenarioCategory.COMPUTE,
					ScenarioLevel.BEGINNER,
					"Choose compute capacity for a growing web service.",
					"Compare small and larger compute choices before traffic increases.",
					List.of(
							ScenarioOption.newOption("Small instance", "Lower cost with limited CPU and memory."),
							ScenarioOption.newOption("Large instance", "Higher capacity with a higher hourly cost.")
					)
			));
			seedPort.save(Scenario.newScenario(
					"Separate public and private traffic",
					ScenarioCategory.NETWORK,
					ScenarioLevel.BEGINNER,
					"Place workloads across public and private network areas.",
					"Decide how a simple service should expose only the required network surface.",
					List.of(
							ScenarioOption.newOption("Public subnet", "Expose resources directly to inbound internet traffic."),
							ScenarioOption.newOption("Private subnet", "Keep resources reachable only through internal routing.")
					)
			));
		};
	}
}
