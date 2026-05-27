package com.yunhwan.cloudsimlab.learningdocument.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.in.GetLearningDocumentUseCase;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
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
	public List<Scenario> findRelatedScenarios(DocumentCategory category) {
		return scenarioCategoryFor(category)
				.map(scenarioCategory -> scenarioQueryPort.findAll(scenarioCategory, null))
				.orElseGet(List::of);
	}

	private Optional<ScenarioCategory> scenarioCategoryFor(DocumentCategory category) {
		if (category == null) {
			return Optional.empty();
		}
		return switch (category) {
			case COMPUTE -> Optional.of(ScenarioCategory.COMPUTE);
			case NETWORK -> Optional.of(ScenarioCategory.NETWORK);
			case STORAGE -> Optional.of(ScenarioCategory.STORAGE);
			case SECURITY -> Optional.of(ScenarioCategory.SECURITY);
		};
	}
}
