package me.drawethree.ultraprisoncore.multipliers.multiplier;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class Multiplier {

	protected double multiplier;

	protected long startTime;

	@Setter
	protected long endTime;

	Multiplier(double multiplier, TimeUnit timeUnit, long duration) {
		this.multiplier = multiplier;
		this.startTime = System.currentTimeMillis();
		this.endTime = startTime + timeUnit.toMillis(duration);
	}

	Multiplier(double multiplier, long endTime) {
		this.multiplier = multiplier;
		this.startTime = System.currentTimeMillis();
		this.endTime = endTime;
	}

	public String getTimeLeftString() {

		if (System.currentTimeMillis() > this.endTime) {
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

		return ChatColor.GRAY + "(" + ChatColor.WHITE + days + "d " + hours + "h " + minutes + "m " + seconds + "s" + ChatColor.GRAY + ")";
	}

	public abstract void setEndTime(long endTime);

	public void setMultiplier(double amount, double maxMultiplier) {
		this.multiplier = Math.min(amount, maxMultiplier);
	}

	public void addMultiplier(double amount, double maxMultiplier) {

		if (this.multiplier + amount > maxMultiplier) {
			this.multiplier = maxMultiplier;
		} else {
			this.multiplier += amount;
		}
	}

	public abstract void addDuration(TimeUnit timeUnit, int duration);

	public boolean isExpired() {
		return System.currentTimeMillis() > this.endTime;
	}

}
