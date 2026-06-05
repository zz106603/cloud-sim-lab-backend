package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioSeedPort;

@Configuration
@Profile("local")
class ScenarioSeed {

	@Bean
	CommandLineRunner seedScenarios(ScenarioSeedPort seedPort) {
		return args -> {
			if (seedPort.count() > 0) {
				return;
			}

			ScenarioSeedCatalog.scenarios().forEach(seedPort::save);
		};
	}
}
