package com.yunhwan.cloudsimlab.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureEdge;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraph;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureGraphs;
import com.yunhwan.cloudsimlab.scenario.domain.ArchitectureNode;
import com.yunhwan.cloudsimlab.scenario.domain.FailureImpact;
import com.yunhwan.cloudsimlab.scenario.domain.FailureImpactEdge;
import com.yunhwan.cloudsimlab.scenario.domain.FailureImpactFlows;
import com.yunhwan.cloudsimlab.scenario.domain.FailureImpactResult;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

public class ContentIntegrityValidator {

	private static final int MIN_EFFECT = -3;
	private static final int MAX_EFFECT = 3;

	public void validate(List<Scenario> scenarios, Set<String> documentKeys, List<LearningRelation> relations) {
		validate(scenarios, documentKeys, relations, List.of(), List.of());
	}

	public void validate(
			List<Scenario> scenarios,
			Set<String> documentKeys,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules
	) {
		List<String> errors = new ArrayList<>();
		List<Scenario> targetScenarios = scenarios == null ? List.of() : scenarios;
		Set<String> targetDocumentKeys = documentKeys == null ? Set.of() : documentKeys;
		List<LearningRelation> targetRelations = relations == null ? List.of() : relations;
		List<LearningPath> targetPaths = paths == null ? List.of() : paths;
		List<LearningModule> targetModules = modules == null ? List.of() : modules;
		Set<String> scenarioKeys = validateScenarios(targetScenarios, errors);

		validateRelations(targetRelations, scenarioKeys, targetDocumentKeys, errors);
		validateCurriculum(targetPaths, targetModules, targetDocumentKeys, scenarioKeys, errors);

		if (!errors.isEmpty()) {
			throw new ContentIntegrityException("Content integrity validation failed:\n- " + String.join("\n- ", errors));
		}
	}

	private Set<String> validateScenarios(List<Scenario> scenarios, List<String> errors) {
		Set<String> scenarioKeys = new HashSet<>();
		for (Scenario scenario : scenarios) {
			String scenarioLabel = scenarioLabel(scenario);
			String scenarioKey = scenario == null ? null : scenario.getGraphKey();

			validateText(scenarioKey, scenarioLabel, "graphKey", errors);
			validateTrimmed(scenarioKey, scenarioLabel, "graphKey", errors);
			validateLowercase(scenarioKey, scenarioLabel, "graphKey", errors);
			if (hasText(scenarioKey) && !scenarioKeys.add(scenarioKey)) {
				errors.add(scenarioLabel + " graphKey is duplicated: " + scenarioKey);
			}
			if (scenario == null) {
				continue;
			}

			validateText(scenario.getTitle(), scenarioLabel, "title", errors);
			validateText(scenario.getSummary(), scenarioLabel, "summary", errors);
			validateText(scenario.getDescription(), scenarioLabel, "description", errors);
			if (scenario.getCategory() == null) {
				errors.add(scenarioLabel + " category must not be null");
			}
			if (scenario.getLevel() == null) {
				errors.add(scenarioLabel + " level must not be null");
			}
			boolean hasInitialArchitecture = validateInitialArchitecture(scenario, scenarioLabel, errors);
			validateOptions(scenario, scenarioLabel, hasInitialArchitecture, errors);
			if (hasInitialArchitecture) {
				ArchitectureGraph initialGraph = ArchitectureGraphs.initialFor(scenario);
				validateGraphEdges(scenarioLabel + " initialArchitecture", initialGraph, errors);
				validateFailureImpact(scenarioLabel + " initialFailureImpact", FailureImpactFlows.initialFor(scenario), initialGraph, errors);
			}
		}
		return scenarioKeys;
	}

	private boolean validateInitialArchitecture(Scenario scenario, String scenarioLabel, List<String> errors) {
		List<String> components = scenario.getInitialArchitecture();
		if (components == null) {
			errors.add(scenarioLabel + " initialArchitecture must not be null");
			return false;
		}
		if (components.isEmpty()) {
			errors.add(scenarioLabel + " initialArchitecture must not be empty");
			return false;
		}
		Set<String> componentNames = new HashSet<>();
		for (String component : components) {
			validateText(component, scenarioLabel, "initialArchitecture", errors);
			validateTrimmed(component, scenarioLabel, "initialArchitecture", errors);
			if (hasText(component) && !componentNames.add(component)) {
				errors.add(scenarioLabel + " initialArchitecture has duplicated component: " + component);
			}
		}
		return true;
	}

	private void validateOptions(Scenario scenario, String scenarioLabel, boolean hasInitialArchitecture, List<String> errors) {
		List<ScenarioOption> options = scenario.getOptions();
		if (options == null) {
			errors.add(scenarioLabel + " options must not be null");
			return;
		}
		if (options.isEmpty()) {
			errors.add(scenarioLabel + " options must not be empty");
			return;
		}

		Set<String> optionGraphKeys = new HashSet<>();
		boolean hasJudgementOption = false;
		for (ScenarioOption option : options) {
			String optionLabel = scenarioLabel + " option[" + optionName(option) + "]";
			if (option == null) {
				errors.add(optionLabel + " must not be null");
				continue;
			}
			validateText(option.getName(), optionLabel, "name", errors);
			validateText(option.getDescription(), optionLabel, "description", errors);
			validateEffects(option, optionLabel, errors);
			hasJudgementOption = hasJudgementOption || option.isCore() || option.getScore() > 0;

			String optionGraphKey = option.getGraphKey();
			if (optionGraphKey == null) {
				continue;
			}
			validateText(optionGraphKey, optionLabel, "graphKey", errors);
			validateTrimmed(optionGraphKey, optionLabel, "graphKey", errors);
			validateLowercase(optionGraphKey, optionLabel, "graphKey", errors);
			if (hasText(optionGraphKey) && !optionGraphKeys.add(optionGraphKey)) {
				errors.add(optionLabel + " graphKey is duplicated in scenario: " + optionGraphKey);
			}
			validateMappedOption(scenario, option, optionLabel, hasInitialArchitecture, errors);
		}

		if (!hasJudgementOption) {
			errors.add(scenarioLabel + " options must include a core option or a positive score option");
		}
	}

	private void validateEffects(ScenarioOption option, String optionLabel, List<String> errors) {
		TradeOffEffects effects = option.getEffects();
		if (effects == null) {
			errors.add(optionLabel + " effects must not be null");
			return;
		}
		validateEffectValue(optionLabel, "performance", effects.performance(), errors);
		validateEffectValue(optionLabel, "availability", effects.availability(), errors);
		validateEffectValue(optionLabel, "cost", effects.cost(), errors);
		validateEffectValue(optionLabel, "complexity", effects.complexity(), errors);
		validateEffectValue(optionLabel, "consistency", effects.consistency(), errors);
		validateEffectValue(optionLabel, "security", effects.security(), errors);
	}

	private void validateMappedOption(
			Scenario scenario,
			ScenarioOption option,
			String optionLabel,
			boolean hasInitialArchitecture,
			List<String> errors
	) {
		if (!ArchitectureGraphs.hasOptionMapping(scenario.getGraphKey(), option.getGraphKey())) {
			errors.add(optionLabel + " graph mapping is missing: " + scenario.getGraphKey() + "::" + option.getGraphKey());
			return;
		}
		if (!hasInitialArchitecture) {
			return;
		}

		ArchitectureGraph initialGraph = ArchitectureGraphs.initialFor(scenario);
		ArchitectureGraph finalGraph = ArchitectureGraphs.finalFor(scenario, List.of(option));
		if (initialGraph.equals(finalGraph)) {
			errors.add(optionLabel + " graph mapping does not change architecture: " + scenario.getGraphKey() + "::" + option.getGraphKey());
		}
		validateGraphEdges(optionLabel + " finalArchitecture", finalGraph, errors);
		validateFailureImpactResult(
				optionLabel + " failureImpactResult",
				FailureImpactFlows.resultFor(scenario, List.of(option)),
				initialGraph,
				finalGraph,
				errors
		);
	}

	private void validateGraphEdges(String graphLabel, ArchitectureGraph graph, List<String> errors) {
		Set<String> nodeIds = new HashSet<>();
		for (ArchitectureNode node : graph.nodes()) {
			if (!hasText(node.id())) {
				errors.add(graphLabel + " node id must not be blank");
				continue;
			}
			nodeIds.add(node.id());
		}
		for (ArchitectureEdge edge : graph.edges()) {
			if (!nodeIds.contains(edge.source())) {
				errors.add(graphLabel + " edge source does not exist: " + edge.source());
			}
			if (!nodeIds.contains(edge.target())) {
				errors.add(graphLabel + " edge target does not exist: " + edge.target());
			}
		}
	}

	private void validateFailureImpact(String label, FailureImpact impact, ArchitectureGraph graph, List<String> errors) {
		if (impact == null) {
			return;
		}
		Set<String> nodeIds = nodeIds(graph);
		Set<String> edgeKeys = edgeKeys(graph);
		validateFailureImpactText(label, impact, errors);
		validateNodeReference(label, "failureSourceNodeId", impact.failureSourceNodeId(), nodeIds, errors);
		for (String nodeId : impact.affectedNodeIds()) {
			validateNodeReference(label, "affectedNodeIds", nodeId, nodeIds, errors);
		}
		for (FailureImpactEdge edge : impact.affectedEdges()) {
			validateFailureImpactEdge(label + " affectedEdges", edge, nodeIds, edgeKeys, errors);
		}
	}

	private void validateFailureImpactResult(
			String label,
			FailureImpactResult result,
			ArchitectureGraph initialGraph,
			ArchitectureGraph finalGraph,
			List<String> errors
	) {
		if (result == null) {
			return;
		}
		Set<String> finalNodeIds = nodeIds(finalGraph);
		Set<String> finalEdgeKeys = edgeKeys(finalGraph);
		for (FailureImpactEdge edge : result.recoveredEdges()) {
			validateFailureImpactEdge(label + " recoveredEdges", edge, finalNodeIds, finalEdgeKeys, errors);
		}
		validateRemainingImpact(label + " remainingImpact", result.remainingImpact(), initialGraph, finalGraph, errors);
		for (String note : result.postActionNotes()) {
			validateText(note, label, "postActionNotes", errors);
		}
	}

	private void validateRemainingImpact(
			String label,
			FailureImpact impact,
			ArchitectureGraph initialGraph,
			ArchitectureGraph finalGraph,
			List<String> errors
	) {
		if (impact == null) {
			return;
		}
		Set<String> nodeIds = nodeIds(initialGraph);
		nodeIds.addAll(nodeIds(finalGraph));
		Set<String> edgeKeys = edgeKeys(initialGraph);
		edgeKeys.addAll(edgeKeys(finalGraph));
		validateFailureImpactText(label, impact, errors);
		validateNodeReference(label, "failureSourceNodeId", impact.failureSourceNodeId(), nodeIds, errors);
		for (String nodeId : impact.affectedNodeIds()) {
			validateNodeReference(label, "affectedNodeIds", nodeId, nodeIds, errors);
		}
		for (FailureImpactEdge edge : impact.affectedEdges()) {
			validateFailureImpactEdge(label + " affectedEdges", edge, nodeIds, edgeKeys, errors);
		}
	}

	private void validateFailureImpactText(String label, FailureImpact impact, List<String> errors) {
		for (String symptom : impact.userSymptoms()) {
			validateText(symptom, label, "userSymptoms", errors);
		}
		for (String risk : impact.remainingRisks()) {
			validateText(risk, label, "remainingRisks", errors);
		}
	}

	private void validateFailureImpactEdge(
			String label,
			FailureImpactEdge edge,
			Set<String> nodeIds,
			Set<String> edgeKeys,
			List<String> errors
	) {
		if (edge == null) {
			errors.add(label + " must not contain null");
			return;
		}
		validateNodeReference(label, "source", edge.source(), nodeIds, errors);
		validateNodeReference(label, "target", edge.target(), nodeIds, errors);
		validateText(edge.label(), label, "label", errors);
		String edgeKey = edge.source() + "->" + edge.target() + "|" + edge.label();
		if (hasText(edge.source()) && hasText(edge.target()) && hasText(edge.label()) && !edgeKeys.contains(edgeKey)) {
			errors.add(label + " edge does not exist in graph: " + edgeKey);
		}
	}

	private void validateNodeReference(String label, String fieldName, String nodeId, Set<String> nodeIds, List<String> errors) {
		validateText(nodeId, label, fieldName, errors);
		if (hasText(nodeId) && !nodeIds.contains(nodeId)) {
			errors.add(label + " " + fieldName + " references unknown graph node: " + nodeId);
		}
	}

	private Set<String> nodeIds(ArchitectureGraph graph) {
		Set<String> nodeIds = new HashSet<>();
		for (ArchitectureNode node : graph.nodes()) {
			nodeIds.add(node.id());
		}
		return nodeIds;
	}

	private Set<String> edgeKeys(ArchitectureGraph graph) {
		Set<String> edgeKeys = new HashSet<>();
		for (ArchitectureEdge edge : graph.edges()) {
			edgeKeys.add(edge.source() + "->" + edge.target() + "|" + edge.label());
		}
		return edgeKeys;
	}

	private void validateRelations(
			List<LearningRelation> relations,
			Set<String> scenarioKeys,
			Set<String> documentKeys,
			List<String> errors
	) {
		Set<String> relationKeys = new HashSet<>();
		for (LearningRelation relation : relations) {
			if (relation == null) {
				errors.add("relation[null] must not be null");
				continue;
			}
			String relationLabel = "relation[" + relation.scenarioKey() + "::" + relation.documentKey() + "]";
			validateText(relation.scenarioKey(), relationLabel, "scenarioKey", errors);
			validateText(relation.documentKey(), relationLabel, "documentKey", errors);
			validateTrimmed(relation.scenarioKey(), relationLabel, "scenarioKey", errors);
			validateTrimmed(relation.documentKey(), relationLabel, "documentKey", errors);
			validateText(relation.learningReason(), relationLabel, "learningReason", errors);
			validateText(relation.reviewFocus(), relationLabel, "reviewFocus", errors);

			if (hasText(relation.scenarioKey()) && !scenarioKeys.contains(relation.scenarioKey())) {
				errors.add(relationLabel + " references unknown scenarioKey: " + relation.scenarioKey());
			}
			if (hasText(relation.documentKey()) && !documentKeys.contains(relation.documentKey())) {
				errors.add(relationLabel + " references unknown documentKey: " + relation.documentKey());
			}
			String relationKey = relation.scenarioKey() + "::" + relation.documentKey();
			if (!relationKeys.add(relationKey)) {
				errors.add(relationLabel + " is duplicated");
			}
		}
	}

	private void validateCurriculum(
			List<LearningPath> paths,
			List<LearningModule> modules,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			List<String> errors
	) {
		if (paths.isEmpty() && modules.isEmpty()) {
			return;
		}

		Set<String> pathIds = validateLearningPaths(paths, errors);
		validateLearningModules(modules, pathIds, documentKeys, scenarioKeys, errors);
		validateLearningPathModuleReferences(paths, modules, errors);
	}

	private Set<String> validateLearningPaths(List<LearningPath> paths, List<String> errors) {
		Set<String> pathIds = new HashSet<>();
		Set<Integer> orderIndexes = new HashSet<>();
		boolean hasRecommendedPath = false;

		for (LearningPath path : paths) {
			if (path == null) {
				errors.add("learningPath[null] must not be null");
				continue;
			}
			String pathLabel = pathLabel(path);
			validateText(path.id(), pathLabel, "id", errors);
			validateTrimmed(path.id(), pathLabel, "id", errors);
			validateText(path.title(), pathLabel, "title", errors);
			validateText(path.description(), pathLabel, "description", errors);
			validateText(path.targetLevel(), pathLabel, "targetLevel", errors);
			validateText(path.learningGoal(), pathLabel, "learningGoal", errors);
			if (path.orderIndex() < 1) {
				errors.add(pathLabel + " orderIndex must be greater than 0: " + path.orderIndex());
			}
			if (!orderIndexes.add(path.orderIndex())) {
				errors.add(pathLabel + " orderIndex is duplicated: " + path.orderIndex());
			}
			if (hasText(path.id()) && !pathIds.add(path.id())) {
				errors.add(pathLabel + " id is duplicated: " + path.id());
			}
			if (path.moduleIds().isEmpty()) {
				errors.add(pathLabel + " moduleIds must not be empty");
			}
			hasRecommendedPath = hasRecommendedPath || path.recommended();
		}

		if (!hasRecommendedPath) {
			errors.add("learningPaths must include at least one recommended path");
		}
		return pathIds;
	}

	private Set<String> validateLearningModules(
			List<LearningModule> modules,
			Set<String> pathIds,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			List<String> errors
	) {
		Set<String> moduleIds = new HashSet<>();
		Map<String, Set<Integer>> orderIndexesByPathId = new HashMap<>();

		for (LearningModule module : modules) {
			if (module == null) {
				errors.add("learningModule[null] must not be null");
				continue;
			}
			String moduleLabel = moduleLabel(module);
			validateText(module.id(), moduleLabel, "id", errors);
			validateTrimmed(module.id(), moduleLabel, "id", errors);
			validateText(module.pathId(), moduleLabel, "pathId", errors);
			validateTrimmed(module.pathId(), moduleLabel, "pathId", errors);
			validateText(module.title(), moduleLabel, "title", errors);
			validateText(module.description(), moduleLabel, "description", errors);
			if (module.orderIndex() < 1) {
				errors.add(moduleLabel + " orderIndex must be greater than 0: " + module.orderIndex());
			}
			if (hasText(module.pathId()) && !pathIds.contains(module.pathId())) {
				errors.add(moduleLabel + " references unknown pathId: " + module.pathId());
			}
			if (hasText(module.id()) && !moduleIds.add(module.id())) {
				errors.add(moduleLabel + " id is duplicated: " + module.id());
			}
			Set<Integer> pathOrderIndexes = orderIndexesByPathId.computeIfAbsent(module.pathId(), ignored -> new HashSet<>());
			if (!pathOrderIndexes.add(module.orderIndex())) {
				errors.add(moduleLabel + " orderIndex is duplicated in path: " + module.orderIndex());
			}
			validateStringList(module.learningGoals(), moduleLabel, "learningGoals", true, errors);
			validateStringList(module.prerequisites(), moduleLabel, "prerequisites", false, errors);
			validateStringList(module.documentIds(), moduleLabel, "documentIds", false, errors);
			validateStringList(module.relatedScenarioIds(), moduleLabel, "relatedScenarioIds", false, errors);
			validateStringList(module.relatedArchitecturePracticeIds(), moduleLabel, "relatedArchitecturePracticeIds", false, errors);
			if (module.documentIds().isEmpty() && module.relatedScenarioIds().isEmpty()) {
				errors.add(moduleLabel + " must reference at least one document or scenario");
			}
			validateDocumentReferences(module, documentKeys, errors);
			validateScenarioReferences(module, scenarioKeys, errors);
		}

		return moduleIds;
	}

	private void validateLearningPathModuleReferences(List<LearningPath> paths, List<LearningModule> modules, List<String> errors) {
		Map<String, LearningModule> modulesById = new HashMap<>();
		for (LearningModule module : modules) {
			if (module != null && hasText(module.id())) {
				modulesById.putIfAbsent(module.id(), module);
			}
		}
		for (LearningPath path : paths) {
			if (path == null) {
				continue;
			}
			Set<String> pathModuleIds = new HashSet<>();
			for (String moduleId : path.moduleIds()) {
				validateText(moduleId, pathLabel(path), "moduleIds", errors);
				if (!hasText(moduleId)) {
					continue;
				}
				LearningModule module = modulesById.get(moduleId);
				if (module == null) {
					errors.add(pathLabel(path) + " references unknown moduleId: " + moduleId);
				}
				else if (!path.id().equals(module.pathId())) {
					errors.add(pathLabel(path) + " references moduleId with mismatched pathId: " + moduleId);
				}
				if (!pathModuleIds.add(moduleId)) {
					errors.add(pathLabel(path) + " moduleIds has duplicated moduleId: " + moduleId);
				}
			}
		}
	}

	private void validateDocumentReferences(LearningModule module, Set<String> documentKeys, List<String> errors) {
		String moduleLabel = moduleLabel(module);
		for (String documentId : module.documentIds()) {
			if (hasText(documentId) && !documentKeys.contains(documentId)) {
				errors.add(moduleLabel + " references unknown documentId: " + documentId);
			}
		}
	}

	private void validateScenarioReferences(LearningModule module, Set<String> scenarioKeys, List<String> errors) {
		String moduleLabel = moduleLabel(module);
		for (String scenarioId : module.relatedScenarioIds()) {
			if (hasText(scenarioId) && !scenarioKeys.contains(scenarioId)) {
				errors.add(moduleLabel + " references unknown relatedScenarioId: " + scenarioId);
			}
		}
	}

	private void validateStringList(
			List<String> values,
			String label,
			String fieldName,
			boolean required,
			List<String> errors
	) {
		if (values == null) {
			errors.add(label + " " + fieldName + " must not be null");
			return;
		}
		if (required && values.isEmpty()) {
			errors.add(label + " " + fieldName + " must not be empty");
		}
		Set<String> uniqueValues = new HashSet<>();
		for (String value : values) {
			validateText(value, label, fieldName, errors);
			validateTrimmed(value, label, fieldName, errors);
			if (hasText(value) && !uniqueValues.add(value)) {
				errors.add(label + " " + fieldName + " has duplicated value: " + value);
			}
		}
	}

	private void validateEffectValue(String optionLabel, String dimension, int value, List<String> errors) {
		if (value < MIN_EFFECT || value > MAX_EFFECT) {
			errors.add(optionLabel + " effects." + dimension + " must be between " + MIN_EFFECT + " and " + MAX_EFFECT + ": " + value);
		}
	}

	private void validateText(String value, String label, String fieldName, List<String> errors) {
		if (!hasText(value)) {
			errors.add(label + " " + fieldName + " must not be blank");
		}
	}

	private void validateTrimmed(String value, String label, String fieldName, List<String> errors) {
		if (value != null && !value.equals(value.trim())) {
			errors.add(label + " " + fieldName + " must not have surrounding whitespace: '" + value + "'");
		}
	}

	private void validateLowercase(String value, String label, String fieldName, List<String> errors) {
		if (value != null && !value.equals(value.toLowerCase(java.util.Locale.ROOT))) {
			errors.add(label + " " + fieldName + " must be lowercase: '" + value + "'");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private String scenarioLabel(Scenario scenario) {
		if (scenario == null) {
			return "scenario[null]";
		}
		return "scenario[" + scenario.getGraphKey() + "|" + scenario.getTitle() + "]";
	}

	private String optionName(ScenarioOption option) {
		if (option == null) {
			return "null";
		}
		return option.getGraphKey() == null ? option.getName() : option.getGraphKey();
	}

	private String pathLabel(LearningPath path) {
		return "learningPath[" + path.id() + "|" + path.title() + "]";
	}

	private String moduleLabel(LearningModule module) {
		return "learningModule[" + module.id() + "|" + module.title() + "]";
	}
}
