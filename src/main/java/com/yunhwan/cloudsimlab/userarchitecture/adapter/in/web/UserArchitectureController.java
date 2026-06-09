package com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web;

import java.time.Duration;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web.UserArchitectureDtos.CatalogResponse;
import com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web.UserArchitectureDtos.DetailResponse;
import com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web.UserArchitectureDtos.SaveRequest;
import com.yunhwan.cloudsimlab.userarchitecture.adapter.in.web.UserArchitectureDtos.SummaryResponse;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.GetUserArchitectureUseCase;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.in.ManageUserArchitectureUseCase;

@RestController
@RequestMapping("/api/user-architectures")
public class UserArchitectureController {

	private final GetUserArchitectureUseCase getUserArchitectureUseCase;
	private final ManageUserArchitectureUseCase manageUserArchitectureUseCase;

	public UserArchitectureController(
			GetUserArchitectureUseCase getUserArchitectureUseCase,
			ManageUserArchitectureUseCase manageUserArchitectureUseCase
	) {
		this.getUserArchitectureUseCase = getUserArchitectureUseCase;
		this.manageUserArchitectureUseCase = manageUserArchitectureUseCase;
	}

	@GetMapping("/catalog")
	public ResponseEntity<CatalogResponse> catalog() {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
				.body(CatalogResponse.supportedTypes());
	}

	@GetMapping
	public List<SummaryResponse> findAll() {
		return getUserArchitectureUseCase.findAll()
				.stream()
				.map(SummaryResponse::from)
				.toList();
	}

	@GetMapping("/{architectureId}")
	public DetailResponse findOne(@PathVariable String architectureId) {
		return DetailResponse.from(getUserArchitectureUseCase.findOne(architectureId));
	}

	@PostMapping
	public ResponseEntity<DetailResponse> create(@RequestBody SaveRequest request) {
		DetailResponse response = DetailResponse.from(manageUserArchitectureUseCase.create(
				request == null ? null : request.toCreateCommand()
		));
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
						.path("/{architectureId}")
						.buildAndExpand(response.architectureId())
						.toUri())
				.body(response);
	}

	@PutMapping("/{architectureId}")
	public DetailResponse update(@PathVariable String architectureId, @RequestBody SaveRequest request) {
		return DetailResponse.from(manageUserArchitectureUseCase.update(
				architectureId,
				request == null ? null : request.toUpdateCommand()
		));
	}

	@DeleteMapping("/{architectureId}")
	public ResponseEntity<Void> delete(@PathVariable String architectureId) {
		manageUserArchitectureUseCase.delete(architectureId);
		return ResponseEntity.noContent().build();
	}
}
