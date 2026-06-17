package com.yunhwan.cloudsimlab.learningdiscovery.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.learningdiscovery.adapter.in.web.LearningDiscoveryDtos.Response;
import com.yunhwan.cloudsimlab.learningdiscovery.application.port.in.GetLearningDiscoveryUseCase;

@RestController
@RequestMapping("/api/learning-discovery")
public class LearningDiscoveryController {

	private final GetLearningDiscoveryUseCase getLearningDiscoveryUseCase;

	public LearningDiscoveryController(GetLearningDiscoveryUseCase getLearningDiscoveryUseCase) {
		this.getLearningDiscoveryUseCase = getLearningDiscoveryUseCase;
	}

	@GetMapping
	public List<Response> findAll(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String level,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) String resourceType
	) {
		return getLearningDiscoveryUseCase.findAll(category, level, tag, resourceType).stream()
				.map(Response::from)
				.toList();
	}
}
