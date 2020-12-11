package me.drawethree.ultraprisoncore.multipliers.multiplier;

import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;

import java.util.concurrent.TimeUnit;

public class GlobalMultiplier extends Multiplier {

    public GlobalMultiplier(double multiplier, int duration) {
        super(multiplier, duration);
        if (endTime > Time.nowMillis()) {
            if (task != null) {
                task.cancel();
            }
            task = Schedulers.async().runLater(() -> {
                setMultiplier(0.0);
                setEndTime(0);
            }, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public GlobalMultiplier(double multiplier, long timeLeft) {
        super(multiplier, timeLeft);
        if (endTime > Time.nowMillis()) {
            if (task != null) {
                task.cancel();
            }
            task = Schedulers.async().runLater(() -> {
                setMultiplier(0.0);
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
                setMultiplier(0.0);
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
                setMultiplier(0.0);
                setEndTime(0);
            }, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
