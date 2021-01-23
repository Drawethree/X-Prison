package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VoucherFinderEnchant extends UltraPrisonEnchantment {

    private final double chance;
    private final List<CommandWithChance> commandsToExecute;

    public VoucherFinderEnchant(UltraPrisonEnchants instance) {
        super(instance, 20);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = this.loadCommands();

    }

    private List<CommandWithChance> loadCommands() {
        List<CommandWithChance> returnList = new ArrayList<>();
        for (String key : this.plugin.getConfig().get().getConfigurationSection("enchants." + id + ".Commands").getKeys(false)) {
            String cmd = this.plugin.getConfig().get().getString("enchants." + id + ".Commands." + key + ".command");
            double chance = this.plugin.getConfig().get().getDouble("enchants." + id + ".Commands." + key + ".chance");
            returnList.add(new CommandWithChance(cmd, chance));
        }
        return returnList;
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
            CommandWithChance randomCmd = RandomSelector.weighted(this.commandsToExecute, element -> element.getChance()).pick();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.getCommand().replace("%player%", e.getPlayer().getName()));
        }
    }

    @AllArgsConstructor
    @Getter
    private class CommandWithChance {
        private String command;
        private double chance;
    }
}
