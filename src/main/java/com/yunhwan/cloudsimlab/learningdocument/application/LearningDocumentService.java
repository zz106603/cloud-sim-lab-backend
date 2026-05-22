package com.yunhwan.cloudsimlab.learningdocument.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentQueryPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@Service
@Transactional(readOnly = true)
public class LearningDocumentService {

	private final LearningDocumentQueryPort queryPort;

	public LearningDocumentService(LearningDocumentQueryPort queryPort) {
		this.queryPort = queryPort;
	}

	public List<LearningDocument> findAll() {
		return queryPort.findAll();
	}

	public LearningDocument findOne(Long documentId) {
		return queryPort.findById(documentId)
				.orElseThrow(() -> new LearningDocumentNotFoundException(documentId));
	}
}
