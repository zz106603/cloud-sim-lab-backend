package com.yunhwan.cloudsimlab.scenario.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.DetailResponse;
import com.yunhwan.cloudsimlab.scenario.adapter.in.web.ScenarioDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.scenario.application.port.in.GetScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

	private final GetScenarioUseCase getScenarioUseCase;

	public ScenarioController(GetScenarioUseCase getScenarioUseCase) {
		this.getScenarioUseCase = getScenarioUseCase;
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
		return DetailResponse.from(getScenarioUseCase.findOne(scenarioId));
	}
}
