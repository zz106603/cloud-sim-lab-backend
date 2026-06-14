package com.yunhwan.cloudsimlab.learningdocument.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.in.GetLearningDocumentUseCase;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningdocument.domain.RelatedScenario;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelation;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;

@Service
@Transactional(readOnly = true)
public class LearningDocumentService implements GetLearningDocumentUseCase {

	private final LearningDocumentQueryPort queryPort;
	private final ScenarioQueryPort scenarioQueryPort;

	public LearningDocumentService(LearningDocumentQueryPort queryPort, ScenarioQueryPort scenarioQueryPort) {
		this.queryPort = queryPort;
		this.scenarioQueryPort = scenarioQueryPort;
	}

	@Override
	public List<LearningDocument> findAll() {
		return queryPort.findAll();
	}

	@Override
	public LearningDocument findOne(Long documentId) {
		return queryPort.findById(documentId)
				.orElseThrow(() -> new LearningDocumentNotFoundException(documentId));
	}

	@Override
	public List<RelatedScenario> findRelatedScenarios(LearningDocument document) {
		if (document == null || document.getDocumentKey() == null) {
			return List.of();
		}
		List<LearningRelation> relations = LearningRelations.forDocument(document.getDocumentKey());
		if (relations.isEmpty()) {
			return List.of();
		}
		List<String> scenarioKeys = relations.stream()
				.map(LearningRelation::scenarioKey)
				.distinct()
				.toList();
		Map<String, Scenario> scenariosByKey = scenarioQueryPort.findAllByGraphKeyIn(scenarioKeys).stream()
				.filter(scenario -> scenario.getGraphKey() != null)
				.collect(Collectors.toMap(Scenario::getGraphKey, Function.identity()));

		return relations.stream()
				.filter(relation -> scenariosByKey.containsKey(relation.scenarioKey()))
				.map(relation -> new RelatedScenario(
						scenariosByKey.get(relation.scenarioKey()),
						relation.learningReason()
				))
				.toList();
	}

	@Override
	public List<String> findRelatedModuleIds(LearningDocument document) {
		if (document == null || document.getDocumentKey() == null) {
			return List.of();
		}
		return CurriculumSeedCatalog.moduleIdsForDocument(document.getDocumentKey());
	}
}
