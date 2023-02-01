package dev.drawethree.xprison.multipliers.multiplier;

import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import lombok.ToString;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ToString
public class PlayerMultiplier extends Multiplier {

	private final UUID playerUUID;
	private final MultiplierType type;

	//New via command
	public PlayerMultiplier(UUID playerUUID, double multiplier, TimeUnit timeUnit, int duration, MultiplierType type) {
		super(multiplier, timeUnit, duration);
		this.type = type;
		this.playerUUID = playerUUID;
	}

	//From DB
	public PlayerMultiplier(UUID playerUUID, double multiplier, long timeLeft, MultiplierType type) {
		super(multiplier, timeLeft);
		this.type = type;
		this.playerUUID = playerUUID;
	}
}
