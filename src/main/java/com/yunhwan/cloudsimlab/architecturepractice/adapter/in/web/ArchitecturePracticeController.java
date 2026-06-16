package com.yunhwan.cloudsimlab.architecturepractice.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yunhwan.cloudsimlab.architecturepractice.adapter.in.web.ArchitecturePracticeDtos.DetailResponse;
import com.yunhwan.cloudsimlab.architecturepractice.adapter.in.web.ArchitecturePracticeDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.architecturepractice.application.port.in.GetArchitecturePracticeUseCase;

@RestController
@RequestMapping("/api/architecture-practices")
public class ArchitecturePracticeController {

	private final GetArchitecturePracticeUseCase getArchitecturePracticeUseCase;

	public ArchitecturePracticeController(GetArchitecturePracticeUseCase getArchitecturePracticeUseCase) {
		this.getArchitecturePracticeUseCase = getArchitecturePracticeUseCase;
	}

	@GetMapping
	public List<SummaryResponse> findAll() {
		return getArchitecturePracticeUseCase.findAll().stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{practiceId}")
	public DetailResponse findOne(@PathVariable String practiceId) {
		return DetailResponse.from(getArchitecturePracticeUseCase.findOne(practiceId));
	}
}
