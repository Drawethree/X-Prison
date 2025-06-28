package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;

import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class KeyFinderEnchant extends XPrisonEnchantmentAbstract implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private List<String> commandsToExecute;

    public KeyFinderEnchant(XPrisonEnchants instance) {
        super(instance, 15);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Commands");
    }


    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        String randomCmd = this.getRandomCommandToExecute();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.replace("%player%", e.getPlayer().getName()));
    }


    private String getRandomCommandToExecute() {
        return this.commandsToExecute.get(ThreadLocalRandom.current().nextInt(commandsToExecute.size()));
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return this.chance * enchantLevel;
    }

    @Override
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Commands");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }
}
