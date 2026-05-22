package com.yunhwan.cloudsimlab.learningdocument.application;

public class LearningDocumentNotFoundException extends RuntimeException {

	public LearningDocumentNotFoundException(Long documentId) {
		super("Learning document not found: " + documentId);
	}
}
