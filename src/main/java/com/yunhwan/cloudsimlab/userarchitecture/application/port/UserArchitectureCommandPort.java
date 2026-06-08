package com.yunhwan.cloudsimlab.userarchitecture.application.port;

import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;

public interface UserArchitectureCommandPort {

	UserArchitecture save(UserArchitecture architecture);

	boolean existsById(String architectureId);

	void deleteById(String architectureId);
}
