package com.yunhwan.cloudsimlab.scenario.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class TradeOffEffectsTests {

	@Test
	void 선택지_효과는_각_차원별로_최소_3에서_최대_3까지_허용한다() {
		assertThat(new TradeOffEffects(-3, -2, -1, 0, 2, 3))
				.isEqualTo(new TradeOffEffects(-3, -2, -1, 0, 2, 3));
		assertThatThrownBy(() -> new TradeOffEffects(4, 0, 0, 0, 0, 0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("performance effect must be between -3 and 3");
	}

	@Test
	void 요약은_선택지별_효과를_차원별로_합산한다() {
		List<ScenarioOption> options = List.of(
				ScenarioOption.newOption("Redis", "설명", 2, true, 0, new TradeOffEffects(3, 1, -2, -3, -3, 0)),
				ScenarioOption.newOption("Read Replica", "설명", 2, true, 0, new TradeOffEffects(2, 2, -3, -2, -2, 0))
		);

		assertThat(TradeOffSummary.from(options))
				.isEqualTo(new TradeOffSummary(5, 3, -5, -5, -5, 0));
	}

	@Test
	void 선택지_효과가_null이면_중립_효과로_보정한다() {
		ScenarioOption option = ScenarioOption.newOption("기본 선택지", "설명", 1, false, 0, null);

		assertThat(option.getEffects()).isEqualTo(TradeOffEffects.none());
		assertThat(TradeOffSummary.from(List.of(option))).isEqualTo(new TradeOffSummary(0, 0, 0, 0, 0, 0));
	}
}
