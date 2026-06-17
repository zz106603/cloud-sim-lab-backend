package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@Configuration
@Profile("local")
class LearningDocumentSeed {

	private static final String CONTENT_ROOT = "learning-documents/";

	private final LearningDocumentContentLoader contentLoader = new LearningDocumentContentLoader();

	@Bean
	CommandLineRunner seedLearningDocuments(LearningDocumentSeedPort seedPort, PlatformTransactionManager transactionManager) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		return args -> transactionTemplate.executeWithoutResult(status -> {
			if (seedPort.count() > 0) {
				return;
			}

			LearningDocumentSeedCatalog.documents().forEach(document -> seedPort.save(LearningDocument.newDocumentWithReinforcement(
					document.documentKey(),
					document.title(),
					document.category(),
					document.level(),
					document.summary(),
					contentLoader.load(CONTENT_ROOT + document.contentFileName()),
					document.orderIndex(),
					document.prerequisiteDocumentIds(),
					document.conceptTags(),
					document.relatedModuleIds(),
					document.relatedScenarioIds(),
					document.checkpoints(),
					document.recallQuestions()
			)));
		});
	}
}
