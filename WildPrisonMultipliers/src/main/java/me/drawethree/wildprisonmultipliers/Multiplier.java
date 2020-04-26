package me.drawethree.wildprisonmultipliers;

import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class Multiplier {

    private Promise<Void> task;
    private UUID playerUUID;
    @Setter
    private double multiplier;
    private int duration;
    private long startTime;
    private long endTime;

    public Multiplier(UUID playerUUID, double multiplier, int duration) {
        this.playerUUID = playerUUID;
        this.multiplier = multiplier;
        setDuration(duration);
    }

    public String getTimeLeft() {

        if (duration == -1) {
            return "PERMANENT";
        }

        long timeLeft = endTime - System.currentTimeMillis();

        long days = timeLeft / (24 * 60 * 60 * 1000);
        timeLeft -= days * (24 * 60 * 60 * 1000);

        long hours = timeLeft / (60 * 60 * 1000);
        timeLeft -= hours * (60 * 60 * 1000);

        long minutes = timeLeft / (60 * 1000);
        timeLeft -= minutes * (60 * 1000);

        long seconds = timeLeft / (1000);

        timeLeft -= seconds * 1000;

        return new StringBuilder().append(days).append("d ").append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s").toString();
    }


    public static final Multiplier getDefaultMultiplier() {
        return new Multiplier(null, 0.0, -1);
    }

    public void setDuration(int minutes) {
        this.duration = minutes;
        this.startTime = System.currentTimeMillis();
        this.endTime = duration == -1 ? startTime : startTime + TimeUnit.MINUTES.toMillis(duration);

        if (task != null) {
            task.cancel();
        }

        if (duration != -1) {
            task = Schedulers.async().runLater(() -> {
                WildPrisonMultipliers.removePersonalMultiplier(this.playerUUID);
            }, duration, TimeUnit.MINUTES);
        }
    }
}
