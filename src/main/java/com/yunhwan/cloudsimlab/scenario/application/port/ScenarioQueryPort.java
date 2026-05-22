package com.yunhwan.cloudsimlab.scenario.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

public interface ScenarioQueryPort {

	List<Scenario> findAll(ScenarioCategory category, ScenarioLevel level);

	Optional<Scenario> findById(Long scenarioId);
}
