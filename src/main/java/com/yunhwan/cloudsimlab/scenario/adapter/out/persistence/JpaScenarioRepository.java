package com.yunhwan.cloudsimlab.scenario.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yunhwan.cloudsimlab.scenario.domain.ScenarioCategory;
import com.yunhwan.cloudsimlab.scenario.domain.ScenarioLevel;

interface JpaScenarioRepository extends JpaRepository<JpaScenarioEntity, Long> {

	List<JpaScenarioEntity> findByCategory(ScenarioCategory category);

	List<JpaScenarioEntity> findByLevel(ScenarioLevel level);

	List<JpaScenarioEntity> findByCategoryAndLevel(ScenarioCategory category, ScenarioLevel level);

	List<JpaScenarioEntity> findByGraphKeyIn(List<String> graphKeys);
}
