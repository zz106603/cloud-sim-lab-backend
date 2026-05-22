package com.yunhwan.cloudsimlab.scenario.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;

final class ScenarioDtos {

	private ScenarioDtos() {
	}

	record SummaryResponse(Long id, String title, ScenarioCategory category, ScenarioLevel level, String summary) {
		static SummaryResponse from(Scenario scenario) {
			return new SummaryResponse(
					scenario.getId(),
					scenario.getTitle(),
					scenario.getCategory(),
					scenario.getLevel(),
					scenario.getSummary()
			);
		}
	}

	record DetailResponse(
			Long id,
			String title,
			ScenarioCategory category,
			ScenarioLevel level,
			String summary,
			String description,
			List<OptionResponse> options
	) {
		static DetailResponse from(Scenario scenario) {
			return new DetailResponse(
					scenario.getId(),
					scenario.getTitle(),
					scenario.getCategory(),
					scenario.getLevel(),
					scenario.getSummary(),
					scenario.getDescription(),
					scenario.getOptions().stream()
							.map(OptionResponse::from)
							.toList()
			);
		}
	}

	record OptionResponse(Long id, String name, String description) {
		static OptionResponse from(ScenarioOption option) {
			return new OptionResponse(option.getId(), option.getName(), option.getDescription());
		}
	}
}
