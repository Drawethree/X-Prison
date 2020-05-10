package me.drawethree.wildprisoncore.multipliers.multiplier;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.wildprisoncore.multipliers.WildPrisonMultipliers;
import me.lucko.helper.promise.Promise;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class Multiplier {

    protected Promise<Void> task;
    @Setter
    protected double multiplier;
    protected int duration;

    protected long startTime;
    protected long endTime;

    public Multiplier(double multiplier, int duration) {
        this.multiplier = multiplier;
        setDuration(duration);
    }

    public Multiplier(double multiplier, long endTime) {
        this.multiplier = multiplier;
        this.startTime = System.currentTimeMillis();
        this.endTime = endTime;
        this.duration = (int) TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);
    }

    public String getTimeLeft() {

        if (System.currentTimeMillis() > endTime || duration == -1) {
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
        return new Multiplier(0.0, -1);
    }

    public static PlayerMultiplier getDefaultPlayerMultiplier(UUID uuid) {
        return new PlayerMultiplier(uuid, 0.0, -1);
    }

    public static PlayerMultiplier getDefaultPlayerMultiplier() {
        return new PlayerMultiplier(null, 0.0, -1);
    }


    public void setDuration(int minutes) {
        this.duration = minutes;
        this.startTime = System.currentTimeMillis();
        this.endTime = duration == -1 ? startTime : startTime + TimeUnit.MINUTES.toMillis(duration);
    }

    public void addMultiplier(double amount, double maxMultiplier) {
        if ((this.multiplier + amount) > maxMultiplier) {
            this.multiplier = maxMultiplier;
        } else {
            this.multiplier += amount;
        }
    }

    public void addDuration(int minutes) {
        this.duration += minutes;
        this.startTime = System.currentTimeMillis();
        this.endTime = duration == -1 ? startTime : startTime + TimeUnit.MINUTES.toMillis(duration);
    }
}
