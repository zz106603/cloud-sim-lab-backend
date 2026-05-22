package com.yunhwan.cloudsimlab.scenario.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

public interface GetScenarioUseCase {

	List<Scenario> findAll(ScenarioCategory category, ScenarioLevel level);

	Scenario findOne(Long scenarioId);
}
