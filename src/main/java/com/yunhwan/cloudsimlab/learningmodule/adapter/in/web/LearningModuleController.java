package com.yunhwan.cloudsimlab.learningmodule.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.learningmodule.adapter.in.web.LearningModuleDtos.Response;
import com.yunhwan.cloudsimlab.learningmodule.application.port.in.GetLearningModuleUseCase;

@RestController
@RequestMapping("/api/learning-modules")
public class LearningModuleController {

	private final GetLearningModuleUseCase getLearningModuleUseCase;

	public LearningModuleController(GetLearningModuleUseCase getLearningModuleUseCase) {
		this.getLearningModuleUseCase = getLearningModuleUseCase;
	}

	@GetMapping
	public List<Response> findAll() {
		return getLearningModuleUseCase.findAll().stream()
				.map(Response::from)
				.toList();
	}

	@GetMapping("/{moduleId}")
	public Response findOne(@PathVariable String moduleId) {
		return Response.from(getLearningModuleUseCase.findOne(moduleId));
	}
}
