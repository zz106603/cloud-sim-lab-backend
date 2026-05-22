package com.yunhwan.cloudsimlab.scenario.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.application.port.in.GetScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

@Service
@Transactional(readOnly = true)
public class ScenarioService implements GetScenarioUseCase {

	private final ScenarioQueryPort queryPort;

	public ScenarioService(ScenarioQueryPort queryPort) {
		this.queryPort = queryPort;
	}

	@Override
	public List<Scenario> findAll(ScenarioCategory category, ScenarioLevel level) {
		return queryPort.findAll(category, level);
	}

	@Override
	public Scenario findOne(Long scenarioId) {
		return queryPort.findById(scenarioId)
				.orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
	}
}
