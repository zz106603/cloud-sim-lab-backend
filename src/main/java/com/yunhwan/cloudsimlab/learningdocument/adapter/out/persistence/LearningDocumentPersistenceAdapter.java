package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@Component
class LearningDocumentPersistenceAdapter implements LearningDocumentQueryPort, LearningDocumentSeedPort {

	private final JpaLearningDocumentRepository repository;

	LearningDocumentPersistenceAdapter(JpaLearningDocumentRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<LearningDocument> findAll() {
		return repository.findAll()
				.stream()
				.map(JpaLearningDocumentEntity::toDomain)
				.toList();
	}

	@Override
	public Optional<LearningDocument> findById(Long documentId) {
		return repository.findById(documentId)
				.map(JpaLearningDocumentEntity::toDomain);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public LearningDocument save(LearningDocument document) {
		return repository.save(JpaLearningDocumentEntity.from(document))
				.toDomain();
	}
}
