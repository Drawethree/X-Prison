package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class VoucherFinderEnchant extends XPrisonEnchantment {

    private double chance;
    private List<CommandWithChance> commandsToExecute;

    public VoucherFinderEnchant(XPrisonEnchants instance) {
        super(instance, 20);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = this.loadCommands();
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        double chance = getChanceToTrigger(enchantLevel);

        if (chance < ThreadLocalRandom.current().nextDouble(100)) {
            return;
        }

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
