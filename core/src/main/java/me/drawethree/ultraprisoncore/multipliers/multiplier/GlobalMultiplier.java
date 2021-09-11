package me.drawethree.ultraprisoncore.multipliers.multiplier;

import me.lucko.helper.Schedulers;

import java.util.concurrent.TimeUnit;

public class GlobalMultiplier extends Multiplier {

	public GlobalMultiplier(double multiplier, int duration, double maxMulti) {
		super(multiplier, duration);

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}

		Schedulers.async().runRepeating(() -> {
			if (this.isExpired() && this.isValid()) {
				this.reset();
			}
		}, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}

	private boolean isValid() {
		return this.multiplier > 0;
	}

	@Override
	public void setEndTime(long endTime) {
		this.startTime = System.currentTimeMillis();
		this.endTime = endTime;
	}


	@Override
	public void addDuration(TimeUnit unit, int duration) {

		this.startTime = System.currentTimeMillis();

		if (this.endTime < this.startTime) {
			this.endTime = this.startTime;
		}

		this.endTime += unit.toMillis(duration);
	}

	public void reset() {
		this.multiplier = 0.0;
		this.startTime = 0;
		this.endTime = 0;
	}
}
