package com.yunhwan.cloudsimlab.learningdocument.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LearningDocumentContentLoaderTests {

	private final LearningDocumentContentLoader contentLoader = new LearningDocumentContentLoader();

	@Test
	void loadsMarkdownContentFromClasspathResource() {
		String content = contentLoader.load("learning-documents/ec2-compute-capacity.md");

		assertThat(content).contains("EC2는 Spring Boot 같은 애플리케이션이 실제 요청을 처리하는 컴퓨팅 자원입니다.");
	}

	@Test
	void throwsDiagnosticExceptionWhenContentResourceIsMissing() {
		assertThatThrownBy(() -> contentLoader.load("learning-documents/missing.md"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Learning document content resource not found or unreadable: learning-documents/missing.md");
	}
}
