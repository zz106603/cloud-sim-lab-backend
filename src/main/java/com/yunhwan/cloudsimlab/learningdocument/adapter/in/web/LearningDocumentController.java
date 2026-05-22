package com.yunhwan.cloudsimlab.learningdocument.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.learningdocument.application.LearningDocumentNotFoundException;
import com.yunhwan.cloudsimlab.learningdocument.application.LearningDocumentService;
import com.yunhwan.cloudsimlab.learningdocument.adapter.in.web.LearningDocumentDtos.DetailResponse;
import com.yunhwan.cloudsimlab.learningdocument.adapter.in.web.LearningDocumentDtos.SummaryResponse;

@RestController
@RequestMapping("/api/docs")
public class LearningDocumentController {

	private final LearningDocumentService service;

	public LearningDocumentController(LearningDocumentService service) {
		this.service = service;
	}

	@GetMapping
	public List<SummaryResponse> findAll() {
		return service.findAll()
				.stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{documentId}")
	public DetailResponse findOne(@PathVariable Long documentId) {
		return DetailResponse.from(service.findOne(documentId));
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@org.springframework.web.bind.annotation.ExceptionHandler(LearningDocumentNotFoundException.class)
	void handleNotFound() {
	}
}
