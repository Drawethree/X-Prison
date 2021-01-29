package me.drawethree.ultraprisoncore.multipliers.multiplier;

import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.promise.Promise;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class Multiplier {

    protected Promise<Void> task;
    @Setter
    protected double multiplier;

    protected long startTime;
    @Setter protected long endTime;

    public Multiplier(double multiplier, int duration) {
        this.multiplier = multiplier;
        this.startTime = System.currentTimeMillis();
        this.endTime = duration == 0 ? 0 : (startTime + TimeUnit.MINUTES.toMillis(duration));
    }

    public Multiplier(double multiplier, long endTime) {
        this.multiplier = multiplier;
        this.startTime = System.currentTimeMillis();
        this.endTime = endTime;
    }

    public String getTimeLeft() {

        if (System.currentTimeMillis() > endTime) {
            return "";
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

        return new StringBuilder().append(ChatColor.GRAY + "(" + ChatColor.WHITE).append(days).append("d ").append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s").append(ChatColor.GRAY + ")").toString();
    }


    public static final GlobalMultiplier getDefaultGlobalMultiplier() {
        return new GlobalMultiplier(0.0, 0);
    }

    public static PlayerMultiplier getDefaultPlayerMultiplier(UUID uuid) {
        return new PlayerMultiplier(uuid, 0.0, 0);
    }

    public static PlayerMultiplier getDefaultPlayerMultiplier() {
        return new PlayerMultiplier(null, 0.0, 0);
    }


    public abstract void setDuration(long endTime);

    public void addMultiplier(double amount /*double maxMultiplier*/) {
       /* if ( (this.multiplier + amount) > maxMultiplier) {
            this.multiplier = maxMultiplier;
        } else {
            this.multiplier += amount;
        }
        */
        if (this.multiplier + amount > 10000) {
            this.multiplier = 10000;
        } else {
            this.multiplier += amount;
        }
    }

    public abstract void addDuration(int minutes);
}
