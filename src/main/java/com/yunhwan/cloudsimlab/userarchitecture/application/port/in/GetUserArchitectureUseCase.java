package com.yunhwan.cloudsimlab.userarchitecture.application.port.in;

import java.util.List;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;

public interface GetUserArchitectureUseCase {

	List<UserArchitecture> findAll();

	UserArchitecture findOne(String architectureId);
}
