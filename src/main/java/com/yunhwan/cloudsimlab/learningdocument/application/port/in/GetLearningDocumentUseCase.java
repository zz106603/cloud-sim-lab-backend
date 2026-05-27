package com.yunhwan.cloudsimlab.learningdocument.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

public interface GetLearningDocumentUseCase {

	List<LearningDocument> findAll();

	LearningDocument findOne(Long documentId);

	List<Scenario> findRelatedScenarios(Long documentId);
}
