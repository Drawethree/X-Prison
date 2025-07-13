package dev.drawethree.xprison.multipliers.multiplier;

import dev.drawethree.xprison.multipliers.MultiplierType;
import lombok.ToString;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ToString
public class PlayerMultiplierBase extends MultiplierBase implements PlayerMultiplier {

	private final UUID playerUUID;
	private final MultiplierType type;

	//New via command
	public PlayerMultiplierBase(UUID playerUUID, double multiplier, TimeUnit timeUnit, int duration, MultiplierType type) {
		super(multiplier, timeUnit, duration);
		this.type = type;
		this.playerUUID = playerUUID;
	}

	//From DB
	public PlayerMultiplierBase(UUID playerUUID, double multiplier, long timeLeft, MultiplierType type) {
		super(multiplier, timeLeft);
		this.type = type;
		this.playerUUID = playerUUID;
	}

	@Override
	public OfflinePlayer getOfflinePlayer() {
		return Players.getOfflineNullable(playerUUID);
	}
}
