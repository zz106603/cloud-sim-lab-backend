package com.yunhwan.cloudsimlab.userarchitecture.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureCommandPort;
import com.yunhwan.cloudsimlab.userarchitecture.application.port.UserArchitectureQueryPort;
import com.yunhwan.cloudsimlab.userarchitecture.domain.UserArchitecture;

@Component
class UserArchitecturePersistenceAdapter implements UserArchitectureQueryPort, UserArchitectureCommandPort {

	private final JpaUserArchitectureRepository repository;

	UserArchitecturePersistenceAdapter(JpaUserArchitectureRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<UserArchitecture> findAll() {
		return repository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
				.stream()
				.map(JpaUserArchitectureEntity::toDomain)
				.toList();
	}

	@Override
	public Optional<UserArchitecture> findById(String architectureId) {
		return repository.findById(architectureId)
				.map(JpaUserArchitectureEntity::toDomain);
	}

	@Override
	public UserArchitecture save(UserArchitecture architecture) {
		return repository.save(JpaUserArchitectureEntity.from(architecture))
				.toDomain();
	}

	@Override
	public boolean existsById(String architectureId) {
		return repository.existsById(architectureId);
	}

	@Override
	public void deleteById(String architectureId) {
		repository.deleteById(architectureId);
	}
}
