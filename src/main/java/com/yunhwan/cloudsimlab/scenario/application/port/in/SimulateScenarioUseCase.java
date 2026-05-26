package com.yunhwan.cloudsimlab.scenario.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.SimulationResult;

public interface SimulateScenarioUseCase {

	SimulationResult simulate(Long scenarioId, List<Long> selectedOptionIds);
}
