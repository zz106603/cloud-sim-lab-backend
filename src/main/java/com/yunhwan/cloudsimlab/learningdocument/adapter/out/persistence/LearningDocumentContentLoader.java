package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;

class LearningDocumentContentLoader {

	String load(String contentPath) {
		try {
			return new ClassPathResource(contentPath).getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Learning document content resource not found or unreadable: " + contentPath, exception);
		}
	}
}
