package com.yunhwan.cloudsimlab.architecturepractice.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yunhwan.cloudsimlab.architecturepractice.application.port.ArchitecturePracticeQueryPort;
import com.yunhwan.cloudsimlab.architecturepractice.application.port.in.GetArchitecturePracticeUseCase;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;

@Service
@Transactional(readOnly = true)
public class ArchitecturePracticeService implements GetArchitecturePracticeUseCase {

	private final ArchitecturePracticeQueryPort queryPort;

	public ArchitecturePracticeService(ArchitecturePracticeQueryPort queryPort) {
		this.queryPort = queryPort;
	}

	@Override
	public List<ArchitecturePracticeTemplate> findAll() {
		return queryPort.findAll().stream()
				.sorted(Comparator.comparing(ArchitecturePracticeTemplate::id))
				.toList();
	}

	@Override
	public ArchitecturePracticeTemplate findOne(String practiceId) {
		return queryPort.findById(practiceId)
				.orElseThrow(() -> new ArchitecturePracticeNotFoundException(practiceId));
	}
}
