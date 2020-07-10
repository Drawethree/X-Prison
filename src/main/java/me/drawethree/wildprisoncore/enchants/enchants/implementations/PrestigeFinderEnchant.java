package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class PrestigeFinderEnchant extends WildPrisonEnchantment {

    private final double chance;
    private final int minLevels;
    private final int maxLevels;

    public PrestigeFinderEnchant(WildPrisonEnchants instance) {
        super(instance, 16);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.minLevels = plugin.getConfig().get().getInt("enchants." + id + ".Min-Levels");
        this.maxLevels = plugin.getConfig().get().getInt("enchants." + id + ".Max-Levels");
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
            int levels = ThreadLocalRandom.current().nextInt(this.minLevels, this.maxLevels);
            this.plugin.getCore().getRanks().getRankManager().givePrestige(e.getPlayer(), levels);
        }
    }
}
