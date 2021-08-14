package me.drawethree.ultraprisoncore.multipliers.multiplier;

import lombok.ToString;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ToString
public class PlayerMultiplier extends Multiplier {

	private final UUID playerUUID;
	private MultiplierType type;
	private double maxMulti;

	public PlayerMultiplier(UUID playerUUID, double multiplier, int duration, double maxMulti, MultiplierType type) {
		super(multiplier, duration);
		this.maxMulti = maxMulti;
		this.type = type;

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}

		this.playerUUID = playerUUID;
		if (endTime > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				switch (this.type) {
					case SELL:
						UltraPrisonMultipliers.getInstance().removeSellMultiplier(this.playerUUID);
						break;
					case TOKENS:
						UltraPrisonMultipliers.getInstance().removeTokenMultiplier(this.playerUUID);
						break;
				}
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}

	}

	public PlayerMultiplier(UUID playerUUID, double multiplier, long timeLeft, MultiplierType type) {
		super(multiplier, timeLeft);
		this.type = type;

		if (this.multiplier > maxMulti) {
			this.multiplier = maxMulti;
		}

		this.playerUUID = playerUUID;

		if (timeLeft > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				switch (this.type) {
					case SELL:
						UltraPrisonMultipliers.getInstance().removeSellMultiplier(this.playerUUID);
						break;
					case TOKENS:
						UltraPrisonMultipliers.getInstance().removeTokenMultiplier(this.playerUUID);
						break;
				}
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
				switch (this.type) {
					case SELL:
						UltraPrisonMultipliers.getInstance().removeSellMultiplier(this.playerUUID);
						break;
					case TOKENS:
						UltraPrisonMultipliers.getInstance().removeTokenMultiplier(this.playerUUID);
						break;
				}
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void addDuration(int minutes) {

		this.startTime = System.currentTimeMillis();
		this.endTime = this.endTime == 0 ? (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)) : this.endTime + TimeUnit.MINUTES.toMillis(minutes);

		if (endTime > Time.nowMillis()) {
			if (task != null) {
				task.cancel();
			}
			task = Schedulers.async().runLater(() -> {
				switch (this.type) {
					case SELL:
						UltraPrisonMultipliers.getInstance().removeSellMultiplier(this.playerUUID);
						break;
					case TOKENS:
						UltraPrisonMultipliers.getInstance().removeTokenMultiplier(this.playerUUID);
						break;
				}
			}, endTime - Time.nowMillis(), TimeUnit.MILLISECONDS);
		}
	}

	public void reset() {
		if (this.task != null) {
			task.cancel();
		}
		this.multiplier = 0.0;
		this.startTime = 0;
		this.endTime = 0;
	}
}
