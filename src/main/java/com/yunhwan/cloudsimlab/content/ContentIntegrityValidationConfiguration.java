package com.yunhwan.cloudsimlab.content;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence.LearningDocumentSeedCatalog;
import com.yunhwan.cloudsimlab.learningpath.adapter.out.persistence.CurriculumSeedCatalog;
import com.yunhwan.cloudsimlab.learningrelation.domain.LearningRelations;
import com.yunhwan.cloudsimlab.scenario.adapter.out.persistence.ScenarioSeedCatalog;

@Configuration
@Profile("local")
class ContentIntegrityValidationConfiguration {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	CommandLineRunner validateSeedContentIntegrity() {
		return args -> new ContentIntegrityValidator().validate(
				ScenarioSeedCatalog.scenarios(),
				LearningDocumentSeedCatalog.documentKeys(),
				LearningRelations.all(),
				CurriculumSeedCatalog.paths(),
				CurriculumSeedCatalog.modules()
		);
	}
}
