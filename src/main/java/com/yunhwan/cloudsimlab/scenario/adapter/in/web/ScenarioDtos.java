package com.yunhwan.cloudsimlab.scenario.adapter.in.web;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.scenario.domain.RelatedLearningDocument;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResult;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResultType;

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
			String problem,
			List<String> initialArchitecture,
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
					scenario.getDescription(),
					scenario.getInitialArchitecture(),
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

	record SimulateRequest(List<Long> selectedOptionIds) {
	}

	record SimulationResponse(
			Long scenarioId,
			SimulationResultType resultType,
			int score,
			int riskScore,
			String summary,
			String detail,
			List<OptionResponse> selectedOptions,
			List<String> finalArchitecture,
			List<RelatedLearningDocumentResponse> relatedLearningDocuments
	) {
		static SimulationResponse from(SimulationResult result) {
			return new SimulationResponse(
					result.getScenarioId(),
					result.getResultType(),
					result.getScore(),
					result.getRiskScore(),
					result.getSummary(),
					result.getDetail(),
					result.getSelectedOptions().stream()
							.map(OptionResponse::from)
							.toList(),
					result.getFinalArchitecture(),
					result.getRelatedLearningDocuments().stream()
							.map(RelatedLearningDocumentResponse::from)
							.toList()
			);
		}
	}

	record RelatedLearningDocumentResponse(
			Long id,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary
	) {
		static RelatedLearningDocumentResponse from(RelatedLearningDocument document) {
			return new RelatedLearningDocumentResponse(
					document.id(),
					document.title(),
					document.category(),
					document.level(),
					document.summary()
			);
		}
	}
}
