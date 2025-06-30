package dev.drawethree.xprison.enchants.model.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class KeyallsEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private List<String> commandsToExecute;

    public KeyallsEnchant() {
    }


    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        String randomCmd = getRandomCommandToExecute();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.replace("%player%", e.getPlayer().getName()));
    }

    private String getRandomCommandToExecute() {
        return this.commandsToExecute.get(ThreadLocalRandom.current().nextInt(commandsToExecute.size()));
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        this.commandsToExecute = new Gson().fromJson(
                config.get("commands"),
                new TypeToken<List<String>>(){}.getType()
        );
    }

}
