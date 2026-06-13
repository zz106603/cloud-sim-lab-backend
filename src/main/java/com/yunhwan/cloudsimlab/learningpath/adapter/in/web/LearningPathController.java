package com.yunhwan.cloudsimlab.learningpath.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.learningpath.adapter.in.web.LearningPathDtos.DetailResponse;
import com.yunhwan.cloudsimlab.learningpath.adapter.in.web.LearningPathDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.learningpath.application.port.in.GetLearningPathUseCase;
import com.yunhwan.cloudsimlab.learningpath.domain.LearningPath;

@RestController
@RequestMapping("/api/learning-paths")
public class LearningPathController {

	private final GetLearningPathUseCase getLearningPathUseCase;

	public LearningPathController(GetLearningPathUseCase getLearningPathUseCase) {
		this.getLearningPathUseCase = getLearningPathUseCase;
	}

	@GetMapping
	public List<SummaryResponse> findAll() {
		return getLearningPathUseCase.findAll().stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{pathId}")
	public DetailResponse findOne(@PathVariable String pathId) {
		LearningPath path = getLearningPathUseCase.findOne(pathId);
		return DetailResponse.from(path, getLearningPathUseCase.findModules(path));
	}
}
