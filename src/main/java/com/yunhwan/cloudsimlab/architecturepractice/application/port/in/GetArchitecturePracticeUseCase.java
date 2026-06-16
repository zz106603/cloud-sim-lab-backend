package com.yunhwan.cloudsimlab.architecturepractice.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.architecturepractice.domain.ArchitecturePracticeTemplate;

public interface GetArchitecturePracticeUseCase {

	List<ArchitecturePracticeTemplate> findAll();

	ArchitecturePracticeTemplate findOne(String practiceId);
}
