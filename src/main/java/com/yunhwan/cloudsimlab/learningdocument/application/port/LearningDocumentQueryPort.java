package com.yunhwan.cloudsimlab.learningdocument.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

public interface LearningDocumentQueryPort {

	List<LearningDocument> findAll();

	List<LearningDocument> findAllByDocumentKeyIn(List<String> documentKeys);

	Optional<LearningDocument> findById(Long documentId);
}
