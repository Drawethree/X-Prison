package me.drawethree.wildprisonmultipliers.multiplier;

import me.drawethree.wildprisonmultipliers.WildPrisonMultipliers;
import me.lucko.helper.Schedulers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerMultiplier extends Multiplier {

    private final UUID playerUUID;

    public PlayerMultiplier(UUID playerUUID, double multiplier, int duration) {
        super(multiplier, duration);
        this.playerUUID = playerUUID;
    }

    @Override
    public void setDuration(int minutes) {
        super.setDuration(minutes);
        if (duration != -1) {
            task = Schedulers.async().runLater(() -> {
                WildPrisonMultipliers.removePersonalMultiplier(playerUUID);
            }, duration, TimeUnit.MINUTES);
        }
    }
}
