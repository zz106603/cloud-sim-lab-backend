package com.yunhwan.cloudsimlab.learningdocument.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.in.GetLearningDocumentUseCase;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.scenario.application.port.ScenarioQueryPort;
import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;

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
	public List<Scenario> findRelatedScenarios(Long documentId) {
		LearningDocument document = findOne(documentId);
		ScenarioCategory category = ScenarioCategory.valueOf(document.getCategory().name());
		return scenarioQueryPort.findAll(category, null);
	}
}
