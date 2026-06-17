package com.yunhwan.cloudsimlab.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeConnection;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeLevel;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeNode;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;
import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog.SeedDocument;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModule;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivity;
import com.yunhwan.cloudsimlab.learningmodule.domain.LearningModulePracticeActivityType;
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
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioObservationPoint;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioOption;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioPrerequisiteConcept;
import com.yunhwan.cloudsimlab.scenario.domain.TradeOffEffects;

public class ContentIntegrityValidator {

	private static final int MIN_EFFECT = -3;
	private static final int MAX_EFFECT = 3;
	private static final Set<String> JUDGMENT_PERSPECTIVES = Set.of(
			"performance",
			"availability",
			"cost",
			"complexity",
			"consistency",
			"security"
	);
	private static final Set<DocumentCategory> DESIGN_DOCUMENT_CATEGORIES = Set.of(
			DocumentCategory.CLOUD_BASICS,
			DocumentCategory.EC2,
			DocumentCategory.VPC,
			DocumentCategory.SUBNET,
			DocumentCategory.SECURITY_GROUP,
			DocumentCategory.NAT_GATEWAY,
			DocumentCategory.ALB,
			DocumentCategory.AUTO_SCALING,
			DocumentCategory.RDS,
			DocumentCategory.READ_REPLICA,
			DocumentCategory.REDIS,
			DocumentCategory.DATA_CONSISTENCY,
			DocumentCategory.CONCURRENCY,
			DocumentCategory.FAILURE_RESPONSE,
			DocumentCategory.CI_CD
	);

	public void validate(List<Scenario> scenarios, Set<String> documentKeys, List<LearningRelation> relations) {
		validate(scenarios, documentKeys, relations, List.of(), List.of(), null, false);
	}

	public void validate(
			List<Scenario> scenarios,
			Set<String> documentKeys,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules
	) {
		validate(scenarios, documentKeys, relations, paths, modules, null, true);
	}

	public void validate(
			List<Scenario> scenarios,
			Set<String> documentKeys,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules,
			List<ArchitecturePracticeTemplate> architecturePractices
	) {
		validate(scenarios, documentKeys, relations, paths, modules, architecturePractices, true);
	}

	private void validate(
			List<Scenario> scenarios,
			Set<String> documentKeys,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules,
			List<ArchitecturePracticeTemplate> architecturePractices,
			boolean validateScenarioModuleReferences
	) {
		List<String> errors = new ArrayList<>();
		List<Scenario> targetScenarios = scenarios == null ? List.of() : scenarios;
		Set<String> targetDocumentKeys = documentKeys == null ? Set.of() : documentKeys;
		List<LearningRelation> targetRelations = relations == null ? List.of() : relations;
		List<LearningPath> targetPaths = paths == null ? List.of() : paths;
		List<LearningModule> targetModules = modules == null ? List.of() : modules;
		List<ArchitecturePracticeTemplate> targetArchitecturePractices = architecturePractices == null ? null : architecturePractices;
		Set<String> scenarioKeys = validateScenarios(targetScenarios, errors);

		validateRelations(targetRelations, scenarioKeys, targetDocumentKeys, errors);
		Set<String> moduleIds = validateCurriculum(
				targetPaths,
				targetModules,
				targetDocumentKeys,
				scenarioKeys,
				architecturePracticeIds(targetArchitecturePractices),
				errors
		);
		validateArchitecturePracticesIfRequested(targetArchitecturePractices, targetDocumentKeys, scenarioKeys, moduleIds, errors);
		validateScenarioLearningContext(targetScenarios, targetDocumentKeys, targetModules, validateScenarioModuleReferences, errors);

		if (!errors.isEmpty()) {
			throw new ContentIntegrityException("Content integrity validation failed:\n- " + String.join("\n- ", errors));
		}
	}

	public void validate(
			List<Scenario> scenarios,
			List<SeedDocument> documents,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules
	) {
		validate(scenarios, documents, relations, paths, modules, null);
	}

	public void validate(
			List<Scenario> scenarios,
			List<SeedDocument> documents,
			List<LearningRelation> relations,
			List<LearningPath> paths,
			List<LearningModule> modules,
			List<ArchitecturePracticeTemplate> architecturePractices
	) {
		List<String> errors = new ArrayList<>();
		List<Scenario> targetScenarios = scenarios == null ? List.of() : scenarios;
		List<SeedDocument> targetDocuments = documents == null ? List.of() : documents;
		List<LearningRelation> targetRelations = relations == null ? List.of() : relations;
		List<LearningPath> targetPaths = paths == null ? List.of() : paths;
		List<LearningModule> targetModules = modules == null ? List.of() : modules;
		List<ArchitecturePracticeTemplate> targetArchitecturePractices = architecturePractices == null ? null : architecturePractices;
		Set<String> scenarioKeys = validateScenarios(targetScenarios, errors);
		Set<String> documentKeys = validateLearningDocuments(targetDocuments, errors);

		validateRelations(targetRelations, scenarioKeys, documentKeys, errors);
		Set<String> moduleIds = validateCurriculum(
				targetPaths,
				targetModules,
				documentKeys,
				scenarioKeys,
				architecturePracticeIds(targetArchitecturePractices),
				errors
		);
		validateLearningDocumentReferences(targetDocuments, targetRelations, targetModules, scenarioKeys, errors);
		validateArchitecturePracticesIfRequested(targetArchitecturePractices, documentKeys, scenarioKeys, moduleIds, errors);
		validateScenarioLearningContext(targetScenarios, documentKeys, targetModules, true, errors);

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
			validateStringList(scenario.getRelatedModuleIds(), scenarioLabel, "relatedModuleIds", true, errors);
			validatePrerequisiteConcepts(scenario, scenarioLabel, errors);
			validateObservationPoint(scenario, scenarioLabel, errors);
			validateStringList(scenario.getJudgmentPerspectives(), scenarioLabel, "judgmentPerspectives", true, errors);
			validateJudgmentPerspectives(scenario, scenarioLabel, errors);
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

	private void validatePrerequisiteConcepts(Scenario scenario, String scenarioLabel, List<String> errors) {
		List<ScenarioPrerequisiteConcept> concepts = scenario.getPrerequisiteConcepts();
		if (concepts == null) {
			errors.add(scenarioLabel + " prerequisiteConcepts must not be null");
			return;
		}
		if (concepts.isEmpty()) {
			errors.add(scenarioLabel + " prerequisiteConcepts must not be empty");
			return;
		}

		Set<String> conceptIds = new HashSet<>();
		for (ScenarioPrerequisiteConcept concept : concepts) {
			String conceptLabel = scenarioLabel + " prerequisiteConcept[" + (concept == null ? "null" : concept.conceptId()) + "]";
			if (concept == null) {
				errors.add(conceptLabel + " must not be null");
				continue;
			}
			validateText(concept.conceptId(), conceptLabel, "conceptId", errors);
			validateTrimmed(concept.conceptId(), conceptLabel, "conceptId", errors);
			validateText(concept.displayName(), conceptLabel, "displayName", errors);
			validateText(concept.relatedDocumentId(), conceptLabel, "relatedDocumentId", errors);
			validateText(concept.reason(), conceptLabel, "reason", errors);
			if (hasText(concept.conceptId()) && !conceptIds.add(concept.conceptId())) {
				errors.add(conceptLabel + " conceptId is duplicated: " + concept.conceptId());
			}
		}
	}

	private void validateObservationPoint(Scenario scenario, String scenarioLabel, List<String> errors) {
		ScenarioObservationPoint observationPoint = scenario.getObservationPoint();
		if (observationPoint == null) {
			errors.add(scenarioLabel + " observationPoint must not be null");
			return;
		}
		validateText(observationPoint.bottleneckMetric(), scenarioLabel, "observationPoint.bottleneckMetric", errors);
		validateText(observationPoint.failurePoint(), scenarioLabel, "observationPoint.failurePoint", errors);
		validateText(observationPoint.requestFlow(), scenarioLabel, "observationPoint.requestFlow", errors);
		validateText(observationPoint.securityBoundary(), scenarioLabel, "observationPoint.securityBoundary", errors);
		validateText(observationPoint.consistencyRisk(), scenarioLabel, "observationPoint.consistencyRisk", errors);
		validateText(observationPoint.tradeOffSignal(), scenarioLabel, "observationPoint.tradeOffSignal", errors);
	}

	private void validateJudgmentPerspectives(Scenario scenario, String scenarioLabel, List<String> errors) {
		for (String perspective : scenario.getJudgmentPerspectives()) {
			if (hasText(perspective) && !JUDGMENT_PERSPECTIVES.contains(perspective)) {
				errors.add(scenarioLabel + " judgmentPerspectives has unknown perspective: " + perspective);
			}
		}
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

	private Set<String> validateLearningDocuments(List<SeedDocument> documents, List<String> errors) {
		Set<String> documentKeys = new HashSet<>();
		Set<Integer> orderIndexes = new HashSet<>();
		for (SeedDocument document : documents) {
			if (document == null) {
				errors.add("learningDocument[null] must not be null");
				continue;
			}
			String documentLabel = documentLabel(document);
			validateText(document.documentKey(), documentLabel, "documentKey", errors);
			validateTrimmed(document.documentKey(), documentLabel, "documentKey", errors);
			validateText(document.title(), documentLabel, "title", errors);
			if (document.category() == null) {
				errors.add(documentLabel + " category must not be null");
			}
			else if (!DESIGN_DOCUMENT_CATEGORIES.contains(document.category())) {
				errors.add(documentLabel + " category must follow design document categories: " + document.category());
			}
			if (document.level() == null) {
				errors.add(documentLabel + " level must not be null");
			}
			validateText(document.summary(), documentLabel, "summary", errors);
			validateText(document.contentFileName(), documentLabel, "contentFileName", errors);
			if (document.orderIndex() < 1) {
				errors.add(documentLabel + " orderIndex must be greater than 0: " + document.orderIndex());
			}
			if (!orderIndexes.add(document.orderIndex())) {
				errors.add(documentLabel + " orderIndex is duplicated: " + document.orderIndex());
			}
			if (hasText(document.documentKey()) && !documentKeys.add(document.documentKey())) {
				errors.add(documentLabel + " documentKey is duplicated: " + document.documentKey());
			}
			validateStringList(document.prerequisiteDocumentIds(), documentLabel, "prerequisiteDocumentIds", false, errors);
			validateStringList(document.conceptTags(), documentLabel, "conceptTags", true, errors);
			validateStringList(document.relatedModuleIds(), documentLabel, "relatedModuleIds", true, errors);
			validateStringList(document.relatedScenarioIds(), documentLabel, "relatedScenarioIds", true, errors);
		}
		return documentKeys;
	}

	private void validateLearningDocumentReferences(
			List<SeedDocument> documents,
			List<LearningRelation> relations,
			List<LearningModule> modules,
			Set<String> scenarioKeys,
			List<String> errors
	) {
		Set<String> documentKeys = documents.stream()
				.filter(document -> document != null && hasText(document.documentKey()))
				.map(SeedDocument::documentKey)
				.collect(java.util.stream.Collectors.toSet());
		Map<String, LearningModule> modulesById = new HashMap<>();
		for (LearningModule module : modules) {
			if (module != null && hasText(module.id())) {
				modulesById.putIfAbsent(module.id(), module);
			}
		}
		Map<String, Set<String>> relationScenarioKeysByDocumentKey = new HashMap<>();
		for (LearningRelation relation : relations) {
			if (relation != null && hasText(relation.documentKey()) && hasText(relation.scenarioKey())) {
				relationScenarioKeysByDocumentKey.computeIfAbsent(relation.documentKey(), ignored -> new HashSet<>())
						.add(relation.scenarioKey());
			}
		}

		for (SeedDocument document : documents) {
			if (document == null) {
				continue;
			}
			String documentLabel = documentLabel(document);
			boolean hasDocumentKey = hasText(document.documentKey());
			for (String prerequisiteDocumentId : document.prerequisiteDocumentIds()) {
				if (hasText(prerequisiteDocumentId) && !documentKeys.contains(prerequisiteDocumentId)) {
					errors.add(documentLabel + " references unknown prerequisiteDocumentId: " + prerequisiteDocumentId);
				}
			}
			for (String relatedModuleId : document.relatedModuleIds()) {
				LearningModule module = modulesById.get(relatedModuleId);
				if (hasText(relatedModuleId) && module == null) {
					errors.add(documentLabel + " references unknown relatedModuleId: " + relatedModuleId);
				}
				else if (hasDocumentKey && module != null && !module.documentIds().contains(document.documentKey())) {
					errors.add(documentLabel + " relatedModuleId does not include documentId: " + relatedModuleId);
				}
			}
			for (String relatedScenarioId : document.relatedScenarioIds()) {
				if (hasText(relatedScenarioId) && !scenarioKeys.contains(relatedScenarioId)) {
					errors.add(documentLabel + " references unknown relatedScenarioId: " + relatedScenarioId);
				}
			}
			if (hasDocumentKey) {
				Set<String> relationScenarioKeys = relationScenarioKeysByDocumentKey.getOrDefault(document.documentKey(), Set.of());
				Set<String> documentScenarioKeys = new HashSet<>(document.relatedScenarioIds());
				if (!relationScenarioKeys.equals(documentScenarioKeys)) {
					errors.add(documentLabel + " relatedScenarioIds must match explicit learning relations");
				}
			}
		}
	}

	private Set<String> validateCurriculum(
			List<LearningPath> paths,
			List<LearningModule> modules,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			Set<String> architecturePracticeIds,
			List<String> errors
	) {
		if (paths.isEmpty() && modules.isEmpty()) {
			return Set.of();
		}

		Set<String> pathIds = validateLearningPaths(paths, errors);
		Set<String> moduleIds = validateLearningModules(modules, pathIds, documentKeys, scenarioKeys, architecturePracticeIds, errors);
		validateLearningPathModuleReferences(paths, modules, errors);
		return moduleIds;
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
			Set<String> architecturePracticeIds,
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
			validateArchitecturePracticeReferences(module, architecturePracticeIds, errors);
			validatePracticeActivities(module, documentKeys, scenarioKeys, architecturePracticeIds, errors);
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

	private void validateArchitecturePracticeReferences(
			LearningModule module,
			Set<String> architecturePracticeIds,
			List<String> errors
	) {
		String moduleLabel = moduleLabel(module);
		if (architecturePracticeIds == null) {
			return;
		}
		for (String practiceId : module.relatedArchitecturePracticeIds()) {
			if (hasText(practiceId) && !architecturePracticeIds.contains(practiceId)) {
				errors.add(moduleLabel + " references unknown relatedArchitecturePracticeId: " + practiceId);
			}
		}
	}

	private void validatePracticeActivities(
			LearningModule module,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			Set<String> architecturePracticeIds,
			List<String> errors
	) {
		String moduleLabel = moduleLabel(module);
		validateObjectList(module.practiceActivities(), moduleLabel, "practiceActivities", true, errors);
		if (module.practiceActivities() == null) {
			return;
		}
		Set<String> activityIds = new HashSet<>();
		Set<Integer> recommendedOrders = new HashSet<>();
		boolean hasApplyActivity = false;

		for (LearningModulePracticeActivity activity : module.practiceActivities()) {
			if (activity == null) {
				continue;
			}
			String activityLabel = moduleLabel + " practiceActivity[" + activity.id() + "]";
			validateText(activity.id(), activityLabel, "id", errors);
			validateTrimmed(activity.id(), activityLabel, "id", errors);
			validateText(activity.title(), activityLabel, "title", errors);
			validateText(activity.description(), activityLabel, "description", errors);
			validateText(activity.targetResourceId(), activityLabel, "targetResourceId", errors);
			validateTrimmed(activity.targetResourceId(), activityLabel, "targetResourceId", errors);
			if (hasText(activity.id()) && !activityIds.add(activity.id())) {
				errors.add(moduleLabel + " practiceActivities has duplicated id: " + activity.id());
			}
			if (activity.recommendedOrder() < 1) {
				errors.add(activityLabel + " recommendedOrder must be greater than 0: " + activity.recommendedOrder());
			}
			else if (!recommendedOrders.add(activity.recommendedOrder())) {
				errors.add(moduleLabel + " practiceActivities has duplicated recommendedOrder: " + activity.recommendedOrder());
			}
			validatePracticeActivityTarget(module, activity, documentKeys, scenarioKeys, architecturePracticeIds, errors);
			if (activity.type() == LearningModulePracticeActivityType.RUN_SCENARIO
					|| activity.type() == LearningModulePracticeActivityType.BUILD_ARCHITECTURE) {
				hasApplyActivity = true;
			}
		}
		if (!hasApplyActivity) {
			errors.add(moduleLabel + " practiceActivities must include at least one RUN_SCENARIO or BUILD_ARCHITECTURE activity");
		}
	}

	private void validatePracticeActivityTarget(
			LearningModule module,
			LearningModulePracticeActivity activity,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			Set<String> architecturePracticeIds,
			List<String> errors
	) {
		String activityLabel = moduleLabel(module) + " practiceActivity[" + activity.id() + "]";
		if (activity.type() == null) {
			errors.add(activityLabel + " type must not be null");
			return;
		}
		if (activity.type() == LearningModulePracticeActivityType.READ_DOCUMENT) {
			if (hasText(activity.targetResourceId()) && !documentKeys.contains(activity.targetResourceId())) {
				errors.add(activityLabel + " references unknown document targetResourceId: " + activity.targetResourceId());
			}
			if (hasText(activity.targetResourceId()) && !module.documentIds().contains(activity.targetResourceId())) {
				errors.add(activityLabel + " targetResourceId must be included in module documentIds: " + activity.targetResourceId());
			}
			return;
		}
		if (activity.type() == LearningModulePracticeActivityType.RUN_SCENARIO) {
			if (hasText(activity.targetResourceId()) && !scenarioKeys.contains(activity.targetResourceId())) {
				errors.add(activityLabel + " references unknown scenario targetResourceId: " + activity.targetResourceId());
			}
			if (hasText(activity.targetResourceId()) && !module.relatedScenarioIds().contains(activity.targetResourceId())) {
				errors.add(activityLabel + " targetResourceId must be included in module relatedScenarioIds: " + activity.targetResourceId());
			}
			return;
		}
		if (architecturePracticeIds != null && hasText(activity.targetResourceId()) && !architecturePracticeIds.contains(activity.targetResourceId())) {
			errors.add(activityLabel + " references unknown architecture practice targetResourceId: " + activity.targetResourceId());
		}
		if (hasText(activity.targetResourceId()) && !module.relatedArchitecturePracticeIds().contains(activity.targetResourceId())) {
			errors.add(activityLabel + " targetResourceId must be included in module relatedArchitecturePracticeIds: " + activity.targetResourceId());
		}
	}

	private Set<String> architecturePracticeIds(List<ArchitecturePracticeTemplate> architecturePractices) {
		if (architecturePractices == null) {
			return null;
		}
		return architecturePractices.stream()
				.filter(practice -> practice != null && hasText(practice.id()))
				.map(ArchitecturePracticeTemplate::id)
				.collect(Collectors.toSet());
	}

	private void validateArchitecturePracticesIfRequested(
			List<ArchitecturePracticeTemplate> practices,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			Set<String> moduleIds,
			List<String> errors
	) {
		if (practices == null) {
			return;
		}
		validateArchitecturePractices(practices, documentKeys, scenarioKeys, moduleIds, errors);
	}

	private void validateArchitecturePractices(
			List<ArchitecturePracticeTemplate> practices,
			Set<String> documentKeys,
			Set<String> scenarioKeys,
			Set<String> moduleIds,
			List<String> errors
	) {
		Set<String> practiceIds = new HashSet<>();
		for (ArchitecturePracticeTemplate practice : practices) {
			if (practice == null) {
				errors.add("architecturePractice[null] must not be null");
				continue;
			}
			String practiceLabel = architecturePracticeLabel(practice);
			validateText(practice.id(), practiceLabel, "id", errors);
			validateTrimmed(practice.id(), practiceLabel, "id", errors);
			if (hasText(practice.id()) && !practiceIds.add(practice.id())) {
				errors.add(practiceLabel + " id is duplicated: " + practice.id());
			}
			validateText(practice.title(), practiceLabel, "title", errors);
			validateText(practice.description(), practiceLabel, "description", errors);
			validateText(practice.learningGoal(), practiceLabel, "learningGoal", errors);
			if (practice.level() == null) {
				errors.add(practiceLabel + " level must not be null");
			}
			validateStringList(practice.instructions(), practiceLabel, "instructions", true, errors);
			validateStarterNodes(practice, practiceLabel, errors);
			validateStarterConnections(practice, practiceLabel, errors);
			validateRequiredResourceTypes(practice, practiceLabel, errors);
			validateRequiredConnectionTypes(practice, practiceLabel, errors);
			validateStringList(practice.relatedDocumentIds(), practiceLabel, "relatedDocumentIds", true, errors);
			validateStringList(practice.relatedScenarioIds(), practiceLabel, "relatedScenarioIds", true, errors);
			validateStringList(practice.relatedModuleIds(), practiceLabel, "relatedModuleIds", true, errors);
			validateArchitecturePracticeDocumentReferences(practice, practiceLabel, documentKeys, errors);
			validateArchitecturePracticeScenarioReferences(practice, practiceLabel, scenarioKeys, errors);
			validateArchitecturePracticeModuleReferences(practice, practiceLabel, moduleIds, errors);
			if (practice.level() == ArchitecturePracticeLevel.BEGINNER
					&& practice.starterNodes().isEmpty()
					&& practice.requiredResourceTypes().isEmpty()) {
				errors.add(practiceLabel + " beginner practice must include starterNodes or requiredResourceTypes");
			}
		}
	}

	private void validateStarterNodes(ArchitecturePracticeTemplate practice, String practiceLabel, List<String> errors) {
		Set<String> nodeIds = new HashSet<>();
		for (ArchitecturePracticeNode node : practice.starterNodes()) {
			if (node == null) {
				errors.add(practiceLabel + " starterNodes must not contain null");
				continue;
			}
			String nodeLabel = practiceLabel + " starterNode[" + node.id() + "]";
			validateText(node.id(), nodeLabel, "id", errors);
			validateTrimmed(node.id(), nodeLabel, "id", errors);
			validateText(node.displayName(), nodeLabel, "displayName", errors);
			if (node.resourceType() == null) {
				errors.add(nodeLabel + " resourceType must not be null");
			}
			if (hasText(node.id()) && !nodeIds.add(node.id())) {
				errors.add(nodeLabel + " id is duplicated: " + node.id());
			}
		}
	}

	private void validateStarterConnections(ArchitecturePracticeTemplate practice, String practiceLabel, List<String> errors) {
		Set<String> nodeIds = practice.starterNodes().stream()
				.filter(node -> node != null && hasText(node.id()))
				.map(ArchitecturePracticeNode::id)
				.collect(Collectors.toSet());
		Set<String> connectionIds = new HashSet<>();
		for (ArchitecturePracticeConnection connection : practice.starterConnections()) {
			if (connection == null) {
				errors.add(practiceLabel + " starterConnections must not contain null");
				continue;
			}
			String connectionLabel = practiceLabel + " starterConnection[" + connection.id() + "]";
			validateText(connection.id(), connectionLabel, "id", errors);
			validateTrimmed(connection.id(), connectionLabel, "id", errors);
			validateText(connection.sourceNodeId(), connectionLabel, "sourceNodeId", errors);
			validateText(connection.targetNodeId(), connectionLabel, "targetNodeId", errors);
			if (connection.connectionType() == null) {
				errors.add(connectionLabel + " connectionType must not be null");
			}
			if (hasText(connection.id()) && !connectionIds.add(connection.id())) {
				errors.add(connectionLabel + " id is duplicated: " + connection.id());
			}
			if (hasText(connection.sourceNodeId()) && !nodeIds.contains(connection.sourceNodeId())) {
				errors.add(connectionLabel + " sourceNodeId references unknown starterNode: " + connection.sourceNodeId());
			}
			if (hasText(connection.targetNodeId()) && !nodeIds.contains(connection.targetNodeId())) {
				errors.add(connectionLabel + " targetNodeId references unknown starterNode: " + connection.targetNodeId());
			}
			if (hasText(connection.sourceNodeId()) && connection.sourceNodeId().equals(connection.targetNodeId())) {
				errors.add(connectionLabel + " must not connect a node to itself: " + connection.sourceNodeId());
			}
		}
	}

	private void validateRequiredResourceTypes(ArchitecturePracticeTemplate practice, String practiceLabel, List<String> errors) {
		Set<String> resourceTypes = new HashSet<>();
		for (var resourceType : practice.requiredResourceTypes()) {
			if (resourceType == null) {
				errors.add(practiceLabel + " requiredResourceTypes must not contain null");
				continue;
			}
			if (!resourceTypes.add(resourceType.name())) {
				errors.add(practiceLabel + " requiredResourceTypes has duplicated resourceType: " + resourceType.name());
			}
		}
	}

	private void validateRequiredConnectionTypes(ArchitecturePracticeTemplate practice, String practiceLabel, List<String> errors) {
		Set<String> connectionTypes = new HashSet<>();
		for (var connectionType : practice.requiredConnectionTypes()) {
			if (connectionType == null) {
				errors.add(practiceLabel + " requiredConnectionTypes must not contain null");
				continue;
			}
			if (!connectionTypes.add(connectionType.name())) {
				errors.add(practiceLabel + " requiredConnectionTypes has duplicated connectionType: " + connectionType.name());
			}
		}
	}

	private void validateArchitecturePracticeDocumentReferences(
			ArchitecturePracticeTemplate practice,
			String practiceLabel,
			Set<String> documentKeys,
			List<String> errors
	) {
		for (String documentId : practice.relatedDocumentIds()) {
			if (hasText(documentId) && !documentKeys.contains(documentId)) {
				errors.add(practiceLabel + " references unknown relatedDocumentId: " + documentId);
			}
		}
	}

	private void validateArchitecturePracticeScenarioReferences(
			ArchitecturePracticeTemplate practice,
			String practiceLabel,
			Set<String> scenarioKeys,
			List<String> errors
	) {
		for (String scenarioId : practice.relatedScenarioIds()) {
			if (hasText(scenarioId) && !scenarioKeys.contains(scenarioId)) {
				errors.add(practiceLabel + " references unknown relatedScenarioId: " + scenarioId);
			}
		}
	}

	private void validateArchitecturePracticeModuleReferences(
			ArchitecturePracticeTemplate practice,
			String practiceLabel,
			Set<String> moduleIds,
			List<String> errors
	) {
		for (String moduleId : practice.relatedModuleIds()) {
			if (hasText(moduleId) && !moduleIds.contains(moduleId)) {
				errors.add(practiceLabel + " references unknown relatedModuleId: " + moduleId);
			}
		}
	}

	private void validateScenarioLearningContext(
			List<Scenario> scenarios,
			Set<String> documentKeys,
			List<LearningModule> modules,
			boolean validateModuleReferences,
			List<String> errors
	) {
		Map<String, LearningModule> modulesById = new HashMap<>();
		for (LearningModule module : modules) {
			if (module != null && hasText(module.id())) {
				modulesById.putIfAbsent(module.id(), module);
			}
		}

		for (Scenario scenario : scenarios) {
			if (scenario == null) {
				continue;
			}
			String scenarioLabel = scenarioLabel(scenario);
			for (String relatedModuleId : scenario.getRelatedModuleIds()) {
				if (!hasText(relatedModuleId) || !validateModuleReferences) {
					continue;
				}
				LearningModule module = modulesById.get(relatedModuleId);
				if (module == null) {
					errors.add(scenarioLabel + " references unknown relatedModuleId: " + relatedModuleId);
				}
				else if (hasText(scenario.getGraphKey()) && !module.relatedScenarioIds().contains(scenario.getGraphKey())) {
					errors.add(scenarioLabel + " relatedModuleId does not include scenario graphKey: " + relatedModuleId);
				}
			}
			for (ScenarioPrerequisiteConcept concept : scenario.getPrerequisiteConcepts()) {
				if (concept != null && hasText(concept.relatedDocumentId()) && !documentKeys.contains(concept.relatedDocumentId())) {
					errors.add(scenarioLabel + " prerequisiteConcept[" + concept.conceptId() + "] references unknown relatedDocumentId: " + concept.relatedDocumentId());
				}
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

	private void validateObjectList(List<?> values, String label, String fieldName, boolean required, List<String> errors) {
		if (values == null) {
			errors.add(label + " " + fieldName + " must not be null");
			return;
		}
		if (required && values.isEmpty()) {
			errors.add(label + " " + fieldName + " must not be empty");
		}
		for (Object value : values) {
			if (value == null) {
				errors.add(label + " " + fieldName + " must not contain null");
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

	private String documentLabel(SeedDocument document) {
		return "learningDocument[" + document.documentKey() + "|" + document.title() + "]";
	}

	private String architecturePracticeLabel(ArchitecturePracticeTemplate practice) {
		return "architecturePractice[" + practice.id() + "|" + practice.title() + "]";
	}
}
