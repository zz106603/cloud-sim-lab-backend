package com.yunhwan.cloudsimlab.scenario.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.application.port.in.GetScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.application.port.in.SimulateScenarioUseCase;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraphs;
import com.yunhwan.cloudsimlab.scenario.domain.RelatedLearningDocument;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.SimulationReview;
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
	public List<RelatedLearningDocument> findRelatedLearningDocuments(Scenario scenario) {
		return relatedLearningDocumentsFor(scenario, null);
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
		List<String> finalArchitecture = finalArchitectureFor(scenario, selectedOptions);
		return new SimulationResult(
				scenario.getId(),
				resultType,
				score,
				riskScore,
				summaryFor(resultType),
				detailFor(scenario, selectedOptions, resultType, hasCoreOptions, includesCoreOption),
				reviewFor(scenario, selectedOptions, resultType, hasCoreOptions, includesCoreOption),
				selectedOptions,
				finalArchitecture,
				ArchitectureGraphs.finalFor(scenario, selectedOptions),
				relatedLearningDocumentsFor(scenario, resultType)
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

	private List<RelatedLearningDocument> relatedLearningDocumentsFor(Scenario scenario, SimulationResultType resultType) {
		if (scenario == null || scenario.getGraphKey() == null) {
			return List.of();
		}
		Map<String, LearningDocument> documentsByKey = learningDocumentQueryPort.findAll().stream()
				.filter(document -> document.getDocumentKey() != null)
				.collect(Collectors.toMap(LearningDocument::getDocumentKey, Function.identity()));

		return LearningRelations.forScenario(scenario.getGraphKey()).stream()
				.filter(relation -> documentsByKey.containsKey(relation.documentKey()))
				.map(relation -> toRelatedLearningDocument(documentsByKey.get(relation.documentKey()), relation, resultType))
				.toList();
	}

	private RelatedLearningDocument toRelatedLearningDocument(
			LearningDocument document,
			LearningRelation relation,
			SimulationResultType resultType
	) {
		return new RelatedLearningDocument(
				document.getId(),
				document.getTitle(),
				document.getCategory(),
				document.getLevel(),
				document.getSummary(),
				reviewReasonFor(relation, resultType)
		);
	}

	private String reviewReasonFor(LearningRelation relation, SimulationResultType resultType) {
		if (resultType == null) {
			return relation.learningReason();
		}
		return switch (resultType) {
			case GOOD -> "복습 초점: " + relation.reviewFocus();
			case PARTIAL -> "남은 병목과 빠진 선택지를 확인할 초점: " + relation.reviewFocus();
			case RISKY -> "운영 위험과 보완 조건을 재검토할 초점: " + relation.reviewFocus();
			case WRONG -> relation.learningReason() + " 먼저 확인할 초점: " + relation.reviewFocus();
		};
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

	private String detailFor(
			Scenario scenario,
			List<ScenarioOption> selectedOptions,
			SimulationResultType resultType,
			boolean hasCoreOptions,
			boolean includesCoreOption
	) {
		return Stream.of(
						resultReasonFor(scenario, resultType, hasCoreOptions, includesCoreOption),
						selectedOptionFeedbackFor(selectedOptions),
						tradeOffFeedbackFor(selectedOptions),
						nextStepFor(resultType)
				)
				.filter(text -> !text.isBlank())
				.collect(Collectors.joining(" "));
	}

	private SimulationReview reviewFor(
			Scenario scenario,
			List<ScenarioOption> selectedOptions,
			SimulationResultType resultType,
			boolean hasCoreOptions,
			boolean includesCoreOption
	) {
		return new SimulationReview(
				resultReasonFor(scenario, resultType, hasCoreOptions, includesCoreOption),
				strengthsFor(selectedOptions, includesCoreOption),
				limitationsFor(resultType, hasCoreOptions, includesCoreOption, selectedOptions),
				missedTradeOffsFor(selectedOptions),
				nextStepFor(resultType)
		);
	}

	private String resultReasonFor(
			Scenario scenario,
			SimulationResultType resultType,
			boolean hasCoreOptions,
			boolean includesCoreOption
	) {
		String goal = scenario.getSummary();
		return switch (resultType) {
			case GOOD -> {
				String reason = hasCoreOptions && includesCoreOption
						? "핵심 선택지가 포함되어 현재 문제의 주요 원인을 직접 줄입니다."
						: "유효한 선택지가 시나리오 목표에 맞게 현재 문제를 줄입니다.";
				yield "시나리오 목표인 '" + goal + "'에 맞습니다. " + reason;
			}
			case PARTIAL -> {
				String reason = hasCoreOptions && !includesCoreOption
						? "시나리오의 핵심 선택지가 빠져 주요 병목이나 장애 지점이 남습니다."
						: "선택지가 일부 도움이 되지만 목표를 충분히 해결하지 못합니다.";
				yield "시나리오 목표인 '" + goal + "'에는 부분적으로만 맞습니다. " + reason;
			}
			case RISKY -> "시나리오 목표인 '" + goal + "'에 일부 기여하더라도 선택한 구성의 위험 점수가 높아 부작용을 먼저 검토해야 합니다.";
			case WRONG -> "시나리오 목표인 '" + goal + "'의 원인을 직접 줄이는 선택지가 포함되지 않았습니다.";
		};
	}

	private String selectedOptionFeedbackFor(List<ScenarioOption> selectedOptions) {
		return selectedOptions.stream()
				.map(option -> option.getName() + subjectParticleFor(option.getName()) + " " + option.getDescription())
				.collect(Collectors.joining(" ", "선택지별 판단: ", ""));
	}

	private List<String> strengthsFor(List<ScenarioOption> selectedOptions, boolean includesCoreOption) {
		return Stream.of(
						includesCoreOption ? "핵심 선택지를 포함해 현재 시나리오의 주요 목표를 직접 개선합니다." : "",
						selectedOptions.stream().anyMatch(option -> option.getScore() > 0)
								? "선택한 구성은 현재 문제 완화에 기여하는 요소를 포함합니다."
								: ""
				)
				.filter(text -> !text.isBlank())
				.toList();
	}

	private List<String> limitationsFor(
			SimulationResultType resultType,
			boolean hasCoreOptions,
			boolean includesCoreOption,
			List<ScenarioOption> selectedOptions
	) {
		return Stream.of(
						hasCoreOptions && !includesCoreOption ? "핵심 선택지가 빠져 주요 병목이나 장애 지점이 남습니다." : "",
						selectedOptions.stream().anyMatch(option -> option.getScore() <= 0)
								? "일부 선택지는 일반적으로 유효해도 현재 문제 원인에는 직접 맞지 않을 수 있습니다."
								: "",
						resultType == SimulationResultType.RISKY ? "효과가 있더라도 운영 위험을 줄일 보완 조건이 필요합니다." : ""
				)
				.filter(text -> !text.isBlank())
				.toList();
	}

	private List<String> missedTradeOffsFor(List<ScenarioOption> selectedOptions) {
		boolean includesRiskOption = selectedOptions.stream()
				.anyMatch(option -> option.getRiskScore() > 0);
		boolean includesCoreOption = selectedOptions.stream()
				.anyMatch(ScenarioOption::isCore);

		return Stream.of(
						includesRiskOption ? "보안 노출, 단일 장애 지점, 일관성 문제, 비용 낭비 같은 운영 부담을 함께 검토해야 합니다." : "",
						includesCoreOption ? "핵심 선택지의 효과와 함께 비용, 확장 지연, 장애 시 우회 흐름을 비교해야 합니다." : "",
						!includesCoreOption ? "남은 병목이 compute, network, storage, security 중 어디에 있는지 다시 좁혀야 합니다." : ""
				)
				.filter(text -> !text.isBlank())
				.toList();
	}

	private String subjectParticleFor(String text) {
		if (text == null || text.isBlank()) {
			return "은";
		}
		char lastCharacter = text.charAt(text.length() - 1);
		if (lastCharacter < '가' || lastCharacter > '힣') {
			return "는";
		}
		return (lastCharacter - '가') % 28 == 0 ? "는" : "은";
	}

	private String tradeOffFeedbackFor(List<ScenarioOption> selectedOptions) {
		boolean includesCoreOption = selectedOptions.stream()
				.anyMatch(ScenarioOption::isCore);
		boolean includesRiskOption = selectedOptions.stream()
				.anyMatch(option -> option.getRiskScore() > 0);
		boolean includesNoScoreOption = selectedOptions.stream()
				.anyMatch(option -> option.getScore() <= 0);

		List<String> feedback = Stream.of(
						includesCoreOption ? "핵심 선택지는 성능, 가용성, 보안 중 현재 시나리오의 주요 목표를 직접 개선합니다." : "",
						includesRiskOption ? "위험 점수가 있는 선택지는 보안 노출, 단일 장애 지점, 일관성 문제, 비용 낭비 같은 운영 부담을 함께 만들 수 있습니다." : "",
						includesNoScoreOption ? "점수가 없는 선택지는 일반적으로 유효한 기술이어도 현재 문제 원인에는 직접 맞지 않을 수 있습니다." : ""
				)
				.filter(text -> !text.isBlank())
				.toList();
		if (feedback.isEmpty()) {
			return "추가 비용과 운영 복잡도는 선택한 구성의 효과와 함께 비교해야 합니다.";
		}
		return String.join(" ", feedback);
	}

	private String nextStepFor(SimulationResultType resultType) {
		return switch (resultType) {
			case GOOD -> "다음으로 비용, 확장 지연, 장애 시 우회 흐름을 함께 확인하세요.";
			case PARTIAL -> "남은 병목이 compute, network, storage, security 중 어디에 있는지 다시 좁히세요.";
			case RISKY -> "적용 전에 위험을 줄일 보완 선택지나 운영 절차가 필요한지 확인하세요.";
			case WRONG -> "먼저 시나리오의 병목과 장애 지점을 다시 식별한 뒤 그 원인을 직접 줄이는 선택지를 고르세요.";
		};
	}
}
