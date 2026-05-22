package com.yunhwan.cloudsimlab.scenario.application.port;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;

public interface ScenarioSeedPort {

	long count();

	Scenario save(Scenario scenario);
}
