package com.yunhwan.cloudsimlab.scenario.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.DetailResponse;
import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.SimulateRequest;
import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.SimulationResponse;
import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.scenario.application.port.in.GetScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.application.port.in.SimulateScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

	private final GetScenarioUseCase getScenarioUseCase;
	private final SimulateScenarioUseCase simulateScenarioUseCase;

	public ScenarioController(GetScenarioUseCase getScenarioUseCase, SimulateScenarioUseCase simulateScenarioUseCase) {
		this.getScenarioUseCase = getScenarioUseCase;
		this.simulateScenarioUseCase = simulateScenarioUseCase;
	}

	@GetMapping
	public List<SummaryResponse> findAll(
			@RequestParam(required = false) ScenarioCategory category,
			@RequestParam(required = false) ScenarioLevel level
	) {
		return getScenarioUseCase.findAll(category, level)
				.stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{scenarioId}")
	public DetailResponse findOne(@PathVariable Long scenarioId) {
		var scenario = getScenarioUseCase.findOne(scenarioId);
		return DetailResponse.from(scenario, getScenarioUseCase.findRelatedLearningDocuments(scenario));
	}

	@PostMapping("/{scenarioId}/simulate")
	public SimulationResponse simulate(@PathVariable Long scenarioId, @RequestBody SimulateRequest request) {
		return SimulationResponse.from(simulateScenarioUseCase.simulate(scenarioId, request.selectedOptionIds()));
	}
}
