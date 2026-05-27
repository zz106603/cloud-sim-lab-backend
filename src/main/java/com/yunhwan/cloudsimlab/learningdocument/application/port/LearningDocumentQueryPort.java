package com.yunhwan.cloudsimlab.learningdocument.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

public interface LearningDocumentQueryPort {

	List<LearningDocument> findAll();

	List<LearningDocument> findAllByCategory(DocumentCategory category);

	Optional<LearningDocument> findById(Long documentId);
}
