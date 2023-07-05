package dev.drawethree.xprison.multipliers.multiplier;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class Multiplier {

	protected double multiplier;

	@Setter
	protected long endTime;

	Multiplier(double multiplier, TimeUnit timeUnit, long duration) {
		this.multiplier = multiplier;
		this.endTime = System.currentTimeMillis() + timeUnit.toMillis(duration);
	}

	Multiplier(double multiplier, long endTime) {
		this.multiplier = multiplier;
		this.endTime = endTime;
	}

	public String getTimeLeftString() {

		if (System.currentTimeMillis() > this.endTime) {
			return "";
		}

		long timeLeft = this.endTime - System.currentTimeMillis();

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

	public void setMultiplier(double amount) {
		this.multiplier = amount;
	}

	public void addMultiplier(double amount) {
		this.multiplier += amount;
	}

	public void addDuration(TimeUnit unit, int duration) {
		if (this.endTime == 0) {
			this.endTime = System.currentTimeMillis();
		}
		this.endTime += unit.toMillis(duration);
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > this.endTime;
	}

	public boolean isValid() {
		return !this.isExpired() && this.multiplier > 0.0;
	}

	public void reset() {
		this.multiplier = 0.0;
		this.endTime = 0;
	}

}
