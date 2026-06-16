package com.yunhwan.cloudsimlab.architecturepractice.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;

public interface ArchitecturePracticeQueryPort {

	List<ArchitecturePracticeTemplate> findAll();

	Optional<ArchitecturePracticeTemplate> findById(String practiceId);
}
