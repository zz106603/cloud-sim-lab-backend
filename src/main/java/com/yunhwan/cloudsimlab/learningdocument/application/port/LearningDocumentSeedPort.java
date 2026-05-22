package com.yunhwan.cloudsimlab.learningdocument.application.port;

import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

public interface LearningDocumentSeedPort {

	long count();

	LearningDocument save(LearningDocument document);
}
