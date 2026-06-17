package com.yunhwan.cloudsimlab.learningmodule.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record LearningModule(
		String id,
		String pathId,
		String title,
		String description,
		List<String> learningGoals,
		List<String> prerequisites,
		int orderIndex,
		List<String> documentIds,
		List<String> relatedScenarioIds,
		List<String> relatedArchitecturePracticeIds,
		List<LearningModulePracticeActivity> practiceActivities
) {
	public LearningModule {
		id = requireText(id, "id");
		pathId = requireText(pathId, "pathId");
		title = requireText(title, "title");
		description = requireText(description, "description");
		learningGoals = List.copyOf(Objects.requireNonNull(learningGoals, "Learning module learningGoals must not be null"));
		prerequisites = List.copyOf(Objects.requireNonNull(prerequisites, "Learning module prerequisites must not be null"));
		documentIds = List.copyOf(Objects.requireNonNull(documentIds, "Learning module documentIds must not be null"));
		relatedScenarioIds = List.copyOf(Objects.requireNonNull(relatedScenarioIds, "Learning module relatedScenarioIds must not be null"));
		relatedArchitecturePracticeIds = List.copyOf(Objects.requireNonNull(relatedArchitecturePracticeIds, "Learning module relatedArchitecturePracticeIds must not be null"));
		practiceActivities = List.copyOf(Objects.requireNonNull(practiceActivities, "Learning module practiceActivities must not be null"));
	}

	public LearningModule(
			String id,
			String pathId,
			String title,
			String description,
			List<String> learningGoals,
			List<String> prerequisites,
			int orderIndex,
			List<String> documentIds,
			List<String> relatedScenarioIds,
			List<String> relatedArchitecturePracticeIds
	) {
		this(
				id,
				pathId,
				title,
				description,
				learningGoals,
				prerequisites,
				orderIndex,
				documentIds,
				relatedScenarioIds,
				relatedArchitecturePracticeIds,
				defaultPracticeActivities(id, documentIds, relatedScenarioIds, relatedArchitecturePracticeIds)
		);
	}

	private static List<LearningModulePracticeActivity> defaultPracticeActivities(
			String moduleId,
			List<String> documentIds,
			List<String> scenarioIds,
			List<String> architecturePracticeIds
	) {
		List<LearningModulePracticeActivity> activities = new ArrayList<>();
		int order = 1;
		for (String documentId : List.copyOf(Objects.requireNonNull(documentIds, "Learning module documentIds must not be null"))) {
			activities.add(new LearningModulePracticeActivity(
					moduleId + "-read-" + documentId,
					LearningModulePracticeActivityType.READ_DOCUMENT,
					"문서 읽기: " + documentId,
					"모듈 학습에 필요한 개념 문서를 먼저 확인합니다.",
					documentId,
					order++
			));
		}
		for (String scenarioId : List.copyOf(Objects.requireNonNull(scenarioIds, "Learning module relatedScenarioIds must not be null"))) {
			activities.add(new LearningModulePracticeActivity(
					moduleId + "-run-" + scenarioId,
					LearningModulePracticeActivityType.RUN_SCENARIO,
					"시나리오 실행: " + scenarioId,
					"문서에서 학습한 판단 기준을 시나리오 선택으로 적용합니다.",
					scenarioId,
					order++
			));
		}
		for (String practiceId : List.copyOf(Objects.requireNonNull(architecturePracticeIds, "Learning module relatedArchitecturePracticeIds must not be null"))) {
			activities.add(new LearningModulePracticeActivity(
					moduleId + "-build-" + practiceId,
					LearningModulePracticeActivityType.BUILD_ARCHITECTURE,
					"아키텍처 작성: " + practiceId,
					"배운 구조를 직접 구성하고 검증 포인트를 확인합니다.",
					practiceId,
					order++
			));
		}
		return activities;
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Learning module " + fieldName + " must not be blank");
		}
		return value;
	}
}
