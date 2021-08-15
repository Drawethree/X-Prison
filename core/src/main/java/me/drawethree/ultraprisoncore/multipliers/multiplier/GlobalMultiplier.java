package me.drawethree.ultraprisoncore.multipliers.multiplier;

import java.util.concurrent.TimeUnit;

public class GlobalMultiplier extends Multiplier {

	public GlobalMultiplier(double multiplier, int duration, double maxMulti) {
		super(multiplier, duration);

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}
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
}
