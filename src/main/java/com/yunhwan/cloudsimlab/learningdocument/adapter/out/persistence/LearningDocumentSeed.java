package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.yunhwan.cloudsimlab.learningdocument.application.port.LearningDocumentSeedPort;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@Configuration
@Profile("local")
class LearningDocumentSeed {

	@Bean
	CommandLineRunner seedLearningDocuments(LearningDocumentSeedPort seedPort) {
		return args -> {
			if (seedPort.count() > 0) {
				return;
			}

			seedPort.save(LearningDocument.newDocument(
					"Virtual machines and compute capacity",
					DocumentCategory.COMPUTE,
					DocumentLevel.BEGINNER,
					"Understand how virtual machines provide configurable compute capacity.",
					"Virtual machines run application workloads on configurable CPU and memory resources. In CloudSimLab, compute choices affect capacity, cost, and failure tolerance."
			));
			seedPort.save(LearningDocument.newDocument(
					"Private networks and subnets",
					DocumentCategory.NETWORK,
					DocumentLevel.BEGINNER,
					"Learn why cloud architectures separate resources into private network segments.",
					"A virtual network groups cloud resources and controls how traffic flows between public and private areas. Subnets are a common boundary for routing and access decisions."
			));
		};
	}
}
