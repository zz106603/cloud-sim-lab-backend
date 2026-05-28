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

	private static final List<String> ARCHITECTURE_COMPONENT_NAMES = List.of(
			"Client",
			"EC2",
			"RDS",
			"Redis",
			"ALB",
			"Load Balancer",
			"Auto Scaling",
			"NAT Gateway",
			"Security Group",
			"Read Replica",
			"Multi-AZ",
			"Internet Gateway",
			"Public subnet",
			"Private subnet",
			"Application server"
	);

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
		boolean includesCoreOption = selectedOptions.stream()
				.anyMatch(ScenarioOption::isCore);

		boolean satisfiesGoodCriteria = hasCoreOptions ? includesCoreOption : hasUsefulOption;
		SimulationResultType resultType = determineResultType(hasUsefulOption, satisfiesGoodCriteria, riskScore);
		return new SimulationResult(
				scenario.getId(),
				resultType,
				score,
				riskScore,
				summaryFor(resultType),
				detailFor(resultType),
				selectedOptions,
				finalArchitectureFor(scenario, selectedOptions),
				relatedLearningDocumentsFor(scenario)
		);
	}

	private List<String> finalArchitectureFor(Scenario scenario, List<ScenarioOption> selectedOptions) {
		return java.util.stream.Stream.concat(
						scenario.getInitialArchitecture().stream(),
						selectedOptions.stream()
								.flatMap(option -> architectureComponentsIn(option.getName()).stream())
				)
				.distinct()
				.toList();
	}

	private List<String> architectureComponentsIn(String text) {
		return ARCHITECTURE_COMPONENT_NAMES.stream()
				.filter(text::contains)
				.toList();
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
			case GOOD -> "선택한 구성이 시나리오 요구를 잘 해결합니다.";
			case PARTIAL -> "일부 도움이 되지만 핵심 선택지가 빠졌습니다.";
			case RISKY -> "효과는 있지만 위험 요소가 함께 있습니다.";
			case WRONG -> "선택한 구성으로는 문제를 해결하기 어렵습니다.";
		};
	}

	private String detailFor(SimulationResultType resultType) {
		return switch (resultType) {
			case GOOD -> "핵심 선택지가 포함되어 성능, 가용성, 보안 중 시나리오의 주요 목표를 직접 개선합니다. 추가 비용과 운영 복잡도는 모니터링해야 합니다.";
			case PARTIAL -> "일부 효과는 있지만 핵심 병목이나 장애 지점이 남아 있습니다. 비용 대비 개선 범위를 다시 확인하세요.";
			case RISKY -> "문제 해결에는 도움이 되지만 보안 노출, 단일 장애 지점, 일관성 문제 같은 위험이 함께 커질 수 있습니다.";
			case WRONG -> "현재 선택은 시나리오의 주요 원인을 직접 줄이지 못합니다. 병목이 compute, network, storage, security 중 어디인지 먼저 좁히세요.";
		};
	}
}
