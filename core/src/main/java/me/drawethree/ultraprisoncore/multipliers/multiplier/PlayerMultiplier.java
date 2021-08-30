package me.drawethree.ultraprisoncore.multipliers.multiplier;

import lombok.ToString;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ToString
public class PlayerMultiplier extends Multiplier {

	private final UUID playerUUID;
	private MultiplierType type;
	private double maxMulti;

	//New via command
	public PlayerMultiplier(UUID playerUUID, double multiplier, TimeUnit timeUnit, int duration, double maxMulti) {
		super(multiplier, timeUnit, duration);
		this.maxMulti = maxMulti;

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}

		this.playerUUID = playerUUID;
	}

	//Old from DB
	public PlayerMultiplier(UUID playerUUID, double multiplier, long timeLeft, MultiplierType type) {
		super(multiplier, timeLeft);
		this.type = type;
		this.playerUUID = playerUUID;
	}

	@Override
	public void setEndTime(long endTime) {
		this.startTime = System.currentTimeMillis();
		this.endTime = endTime;
	}

	@Override
	public void addDuration(TimeUnit unit, int duration) {
		this.startTime = System.currentTimeMillis();
		this.endTime = this.endTime == 0 ? (System.currentTimeMillis() + unit.toMillis(duration)) : this.endTime + unit.toMillis(duration);
	}

	public void reset() {
		this.multiplier = 0.0;
		this.startTime = 0;
		this.endTime = 0;
	}
}
