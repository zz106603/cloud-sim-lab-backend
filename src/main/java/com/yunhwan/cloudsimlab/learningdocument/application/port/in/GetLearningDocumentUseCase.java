package com.yunhwan.cloudsimlab.learningdocument.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;
import com.yunhwan.cloudsimlab.learningdocument.domain.RelatedScenario;

public interface GetLearningDocumentUseCase {

	List<LearningDocument> findAll();

	LearningDocument findOne(Long documentId);

	List<RelatedScenario> findRelatedScenarios(LearningDocument document);

	List<String> findRelatedModuleIds(LearningDocument document);

	List<String> findRelatedScenarioIds(LearningDocument document);
}
