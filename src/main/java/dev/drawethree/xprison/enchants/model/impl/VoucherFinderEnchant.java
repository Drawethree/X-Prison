package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

public final class VoucherFinderEnchant extends XPrisonEnchantmentAbstract implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private List<CommandWithChance> commandsToExecute;

    public VoucherFinderEnchant(XPrisonEnchants instance) {
        super(instance, 20);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = this.loadCommands();
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
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = this.loadCommands();
    }

    private List<CommandWithChance> loadCommands() {
        List<CommandWithChance> returnList = new ArrayList<>();
        for (String key : this.plugin.getEnchantsConfig().getYamlConfig().getConfigurationSection("enchants." + id + ".Commands").getKeys(false)) {
            String cmd = this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Commands." + key + ".command");
            double chance = this.plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Commands." + key + ".chance");
            returnList.add(new CommandWithChance(cmd, chance));
        }
        return returnList;
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    @AllArgsConstructor
    @Getter
    private static class CommandWithChance {
        private final String command;
        private final double chance;
    }
}
