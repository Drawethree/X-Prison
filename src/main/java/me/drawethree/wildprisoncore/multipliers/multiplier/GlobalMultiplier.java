package me.drawethree.wildprisoncore.multipliers.multiplier;

import me.lucko.helper.Schedulers;

import java.util.concurrent.TimeUnit;

public class GlobalMultiplier extends Multiplier {

    public GlobalMultiplier(double multiplier, int duration) {
        super(multiplier, duration);
    }

    public GlobalMultiplier(double multiplier, long timeLeft) {
        super(multiplier,timeLeft);
    }

    @Override
    public void setDuration(int minutes) {
        super.setDuration(minutes);
        if (duration != -1) {
            task = Schedulers.async().runLater(() -> {
                setMultiplier(0.0);
            }, duration, TimeUnit.MINUTES);
        }
    }

    @Override
    public void addDuration(int minutes) {
        super.addDuration(minutes);
        if (duration != -1) {
            task = Schedulers.async().runLater(() -> {
                setMultiplier(0.0);
            }, duration, TimeUnit.MINUTES);
        }
    }
}
