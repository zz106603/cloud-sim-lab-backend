package com.yunhwan.cloudsimlab.learningdocument.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.learningdocument.adapter.in.web.LearningDocumentDtos.DetailResponse;
import com.yunhwan.cloudsimlab.learningdocument.adapter.in.web.LearningDocumentDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.learningdocument.application.port.in.GetLearningDocumentUseCase;
import com.yunhwan.cloudsimlab.learningdocument.domain.LearningDocument;

@RestController
@RequestMapping("/api/docs")
public class LearningDocumentController {

	private final GetLearningDocumentUseCase getLearningDocumentUseCase;

	public LearningDocumentController(GetLearningDocumentUseCase getLearningDocumentUseCase) {
		this.getLearningDocumentUseCase = getLearningDocumentUseCase;
	}

	@GetMapping
	public List<SummaryResponse> findAll() {
		return getLearningDocumentUseCase.findAll()
				.stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{documentId}")
	public DetailResponse findOne(@PathVariable Long documentId) {
		LearningDocument document = getLearningDocumentUseCase.findOne(documentId);
		return DetailResponse.from(
				document,
				getLearningDocumentUseCase.findRelatedScenarios(document)
		);
	}
}
