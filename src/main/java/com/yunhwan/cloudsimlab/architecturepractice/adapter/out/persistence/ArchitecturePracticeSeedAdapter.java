package com.yunhwan.cloudsimlab.architecturepractice.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.architecturepractice.application.port.ArchitecturePracticeQueryPort;
import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;

@Component
public class ArchitecturePracticeSeedAdapter implements ArchitecturePracticeQueryPort {

	@Override
	public List<ArchitecturePracticeTemplate> findAll() {
		return ArchitecturePracticeSeedCatalog.practices();
	}

	@Override
	public Optional<ArchitecturePracticeTemplate> findById(String practiceId) {
		return ArchitecturePracticeSeedCatalog.practices().stream()
				.filter(practice -> practice.id().equals(practiceId))
				.findFirst();
	}
}
