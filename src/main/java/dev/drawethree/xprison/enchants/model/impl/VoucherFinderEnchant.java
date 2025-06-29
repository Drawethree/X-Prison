package dev.drawethree.xprison.enchants.model.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.enchants.model.RequiresPickaxeLevel;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public final class VoucherFinderEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant, RequiresPickaxeLevel {

    private double chance;
    private List<CommandWithChance> commandsToExecute;
    private int requiredPickaxeLevel;

    public VoucherFinderEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        CommandWithChance randomCmd = getRandomCommandToExecute();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.getCommand().replace("%player%", e.getPlayer().getName()));
    }

    private CommandWithChance getRandomCommandToExecute() {
        return RandomSelector.weighted(this.commandsToExecute, CommandWithChance::getChance).pick();
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return this.chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        JsonElement element = config.get("commands");

        this.commandsToExecute = new Gson().fromJson(
                element,
                new TypeToken<List<CommandWithChance>>() {
                }.getType()
        );
        this.requiredPickaxeLevel = config.get("pickaxeLevelRequired").getAsInt();
    }

    @Override
    public int getRequiredPickaxeLevel() {
        return requiredPickaxeLevel;
    }

    @AllArgsConstructor
    @Getter
    private static class CommandWithChance {
        private final String command;
        private final double chance;
    }
}
