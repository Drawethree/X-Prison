package me.drawethree.ultraprisoncore.multipliers.multiplier;

import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;

import java.util.concurrent.TimeUnit;

public class GlobalMultiplier extends Multiplier {

	private final double maxMulti;

	public GlobalMultiplier(double multiplier, int duration, double maxMulti) {
		super(multiplier, duration);

		this.maxMulti = maxMulti;

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}

		if (endTime > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				setMultiplier(0.0, maxMulti);
				setEndTime(0);
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void setDuration(long endTime) {

		this.startTime = System.currentTimeMillis();
		this.endTime = endTime;

		if (endTime > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				setMultiplier(0.0, maxMulti);
				setEndTime(0);
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}
	}


	@Override
	public void addDuration(int minutes) {

		this.startTime = System.currentTimeMillis();

		if (this.endTime < this.startTime) {
			this.endTime = this.startTime;
		}

		this.endTime += TimeUnit.MINUTES.toMillis(minutes);

		if (endTime > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				setMultiplier(0.0, maxMulti);
				setEndTime(0);
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}
	}
}
