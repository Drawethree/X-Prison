package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MillionaireEnchant extends WildPrisonEnchantment {
    private final double chance;
    private final List<String> commands;

    public MillionaireEnchant(WildPrisonEnchants instance) {
        super(instance, 16);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.commands = plugin.getConfig().get().getStringList("enchants." + id + ".Commands");
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
            String cmdToRun = commands.get(ThreadLocalRandom.current().nextInt(commands.size()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun.replace("%player%", e.getPlayer().getName()));
        }
    }
}
