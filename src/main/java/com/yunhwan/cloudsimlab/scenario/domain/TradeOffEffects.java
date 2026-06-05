package com.yunhwan.cloudsimlab.scenario.domain;

public record TradeOffEffects(
		int performance,
		int availability,
		int cost,
		int complexity,
		int consistency,
		int security
) {

	private static final int MIN_EFFECT = -3;
	private static final int MAX_EFFECT = 3;
	private static final TradeOffEffects NONE = new TradeOffEffects(0, 0, 0, 0, 0, 0);

	public TradeOffEffects {
		validate("performance", performance);
		validate("availability", availability);
		validate("cost", cost);
		validate("complexity", complexity);
		validate("consistency", consistency);
		validate("security", security);
	}

	public static TradeOffEffects none() {
		return NONE;
	}

	private static void validate(String dimension, int value) {
		if (value < MIN_EFFECT || value > MAX_EFFECT) {
			throw new IllegalArgumentException(
					dimension + " effect must be between " + MIN_EFFECT + " and " + MAX_EFFECT
			);
		}
	}
}
