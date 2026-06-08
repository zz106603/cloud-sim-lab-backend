package com.yunhwan.cloudsimlab.userarchitecture.application.port;

import java.util.List;
import java.util.Optional;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;

public interface UserArchitectureQueryPort {

	List<UserArchitecture> findAll();

	Optional<UserArchitecture> findById(String architectureId);
}
