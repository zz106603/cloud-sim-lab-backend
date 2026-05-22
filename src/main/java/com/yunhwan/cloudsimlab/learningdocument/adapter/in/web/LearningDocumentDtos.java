package com.yunhwan.cloudsimlab.learningdocument.adapter.in.web;

import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentCategory;
import com.yunhwan.cloudsimlab.learningdocument.domain.DocumentLevel;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

final class LearningDocumentDtos {

	private LearningDocumentDtos() {
	}

	record SummaryResponse(Long id, String title, DocumentCategory category, DocumentLevel level, String summary) {
		static SummaryResponse from(LearningDocument document) {
			return new SummaryResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary()
			);
		}
	}

	record DetailResponse(
			Long id,
			String title,
			DocumentCategory category,
			DocumentLevel level,
			String summary,
			String content
	) {
		static DetailResponse from(LearningDocument document) {
			return new DetailResponse(
					document.getId(),
					document.getTitle(),
					document.getCategory(),
					document.getLevel(),
					document.getSummary(),
					document.getContent()
			);
		}
	}
}
