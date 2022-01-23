package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class PrestigeFinderEnchant extends UltraPrisonEnchantment {

    private double chance;
    private int minLevels;
    private int maxLevels;

    private String commandToExecute;

    public PrestigeFinderEnchant(UltraPrisonEnchants instance) {
        super(instance, 16);
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
            int levels = minLevels == maxLevels ? minLevels : ThreadLocalRandom.current().nextInt(this.minLevels, this.maxLevels);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute.replace("%player%", e.getPlayer().getName()).replace("%amount%", String.valueOf(levels)));
        }
    }

    @Override
    public void reload() {
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.minLevels = plugin.getConfig().get().getInt("enchants." + id + ".Min-Levels");
        this.maxLevels = plugin.getConfig().get().getInt("enchants." + id + ".Max-Levels");
        this.commandToExecute = plugin.getConfig().get().getString("enchants." + id + ".Command");

    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }
}
