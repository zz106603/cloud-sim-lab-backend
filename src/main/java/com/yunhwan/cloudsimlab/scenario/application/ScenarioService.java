package com.yunhwan.cloudsimlab.scenario.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.application.port.in.GetScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.application.port.in.SimulateScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.domain.RelatedLearningDocument;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResult;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationResultType;

@Service
@Transactional(readOnly = true)
public class ScenarioService implements GetScenarioUseCase, SimulateScenarioUseCase {

	private final ScenarioQueryPort queryPort;
	private final LearningDocumentQueryPort learningDocumentQueryPort;

	public ScenarioService(ScenarioQueryPort queryPort, LearningDocumentQueryPort learningDocumentQueryPort) {
		this.queryPort = queryPort;
		this.learningDocumentQueryPort = learningDocumentQueryPort;
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

	@Override
	public SimulationResult simulate(Long scenarioId, List<Long> selectedOptionIds) {
		if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
			throw new InvalidSimulationRequestException("selectedOptionIds must not be empty");
		}
		if (selectedOptionIds.stream().anyMatch(optionId -> optionId == null)) {
			throw new InvalidSimulationRequestException("selectedOptionIds must not contain null");
		}

		Scenario scenario = findOne(scenarioId);
		Map<Long, ScenarioOption> optionsById = scenario.getOptions().stream()
				.collect(Collectors.toMap(ScenarioOption::getId, Function.identity()));
		Set<Long> selectedIds = new LinkedHashSet<>(selectedOptionIds);
		List<Long> unknownOptionIds = selectedIds.stream()
				.filter(optionId -> !optionsById.containsKey(optionId))
				.toList();

		if (!unknownOptionIds.isEmpty()) {
			throw new InvalidSimulationRequestException("Unknown scenario option IDs: " + unknownOptionIds);
		}

		List<ScenarioOption> selectedOptions = selectedIds.stream()
				.map(optionsById::get)
				.toList();
		int score = selectedOptions.stream()
				.mapToInt(ScenarioOption::getScore)
				.sum();
		int riskScore = selectedOptions.stream()
				.mapToInt(ScenarioOption::getRiskScore)
				.sum();
		boolean hasUsefulOption = selectedOptions.stream()
				.anyMatch(option -> option.getScore() > 0 || option.isCore());
		boolean hasCoreOptions = scenario.getOptions().stream()
				.anyMatch(ScenarioOption::isCore);
		boolean includesAllCoreOptions = scenario.getOptions().stream()
				.filter(ScenarioOption::isCore)
				.allMatch(option -> selectedIds.contains(option.getId()));

		boolean satisfiesGoodCriteria = hasCoreOptions ? includesAllCoreOptions : hasUsefulOption;
		SimulationResultType resultType = determineResultType(hasUsefulOption, satisfiesGoodCriteria, riskScore);
		return new SimulationResult(
				scenario.getId(),
				resultType,
				score,
				riskScore,
				summaryFor(resultType),
				detailFor(resultType),
				selectedOptions,
				relatedLearningDocumentsFor(scenario)
		);
	}

	private List<RelatedLearningDocument> relatedLearningDocumentsFor(Scenario scenario) {
		DocumentCategory category = documentCategoryFor(scenario.getCategory());
		if (category == null) {
			return List.of();
		}
		return learningDocumentQueryPort.findAllByCategory(category).stream()
				.map(this::toRelatedLearningDocument)
				.toList();
	}

	private DocumentCategory documentCategoryFor(ScenarioCategory category) {
		if (category == null) {
			return null;
		}
		return switch (category) {
			case COMPUTE -> DocumentCategory.COMPUTE;
			case NETWORK -> DocumentCategory.NETWORK;
			case STORAGE -> DocumentCategory.STORAGE;
			case SECURITY -> DocumentCategory.SECURITY;
		};
	}

	private RelatedLearningDocument toRelatedLearningDocument(LearningDocument document) {
		return new RelatedLearningDocument(
				document.getId(),
				document.getTitle(),
				document.getCategory(),
				document.getLevel(),
				document.getSummary()
		);
	}

	private SimulationResultType determineResultType(boolean hasUsefulOption, boolean satisfiesGoodCriteria, int riskScore) {
		if (!hasUsefulOption) {
			return SimulationResultType.WRONG;
		}
		if (riskScore >= 2) {
			return SimulationResultType.RISKY;
		}
		if (satisfiesGoodCriteria) {
			return SimulationResultType.GOOD;
		}
		return SimulationResultType.PARTIAL;
	}

	private String summaryFor(SimulationResultType resultType) {
		return switch (resultType) {
			case GOOD -> "The selected options address the scenario well.";
			case PARTIAL -> "The selected options help, but miss a core choice.";
			case RISKY -> "The selected options include useful choices but introduce risk.";
			case WRONG -> "The selected options do not address the scenario.";
		};
	}

	private String detailFor(SimulationResultType resultType) {
		return switch (resultType) {
			case GOOD -> "Core options are included and no high-risk tradeoff dominates the result.";
			case PARTIAL -> "At least one useful option was selected, but the scenario still lacks a required core option.";
			case RISKY -> "The result has useful impact, but the selected high-risk option can make the architecture fragile.";
			case WRONG -> "Choose an option that directly improves the scenario goal.";
		};
	}
}
