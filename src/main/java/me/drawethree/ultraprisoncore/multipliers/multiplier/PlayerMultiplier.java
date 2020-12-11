package me.drawethree.ultraprisoncore.multipliers.multiplier;

import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerMultiplier extends Multiplier {

    private final UUID playerUUID;

    public PlayerMultiplier(UUID playerUUID, double multiplier, int duration) {
        super(multiplier, duration);
        this.playerUUID = playerUUID;
        if (endTime > Time.nowMillis()) {
            if (task != null) {
                task.cancel();
            }
            task = Schedulers.async().runLater(() -> {
                UltraPrisonMultipliers.getInstance().removePersonalMultiplier(this.playerUUID);
            }, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public PlayerMultiplier(UUID playerUUID, double multiplier, long timeLeft) {
        super(multiplier, timeLeft);
        this.playerUUID = playerUUID;

        if (timeLeft > Time.nowMillis()) {
            if (task != null) {
                task.cancel();
            }
            task = Schedulers.async().runLater(() -> {
                UltraPrisonMultipliers.getInstance().removePersonalMultiplier(this.playerUUID);
            }, timeLeft - Time.nowMillis(), TimeUnit.MILLISECONDS);
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
                UltraPrisonMultipliers.getInstance().removePersonalMultiplier(this.playerUUID);
            }, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void addDuration(int minutes) {

        this.startTime = System.currentTimeMillis();
        this.endTime += TimeUnit.MINUTES.toMillis(minutes);

        if (endTime > Time.nowMillis()) {
            if (task != null) {
                task.cancel();
            }
            task = Schedulers.async().runLater(() -> {
                UltraPrisonMultipliers.getInstance().removePersonalMultiplier(this.playerUUID);
            }, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
