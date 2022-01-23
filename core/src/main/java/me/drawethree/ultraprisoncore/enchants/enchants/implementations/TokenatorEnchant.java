package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class TokenatorEnchant extends UltraPrisonEnchantment {

	private long maxAmount;
    private long minAmount;
    private double chance;


    public TokenatorEnchant(UltraPrisonEnchants instance) {
        super(instance, 14);
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
            long randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
            plugin.getCore().getTokens().getTokensManager().giveTokens(e.getPlayer(), randAmount, null, ReceiveCause.MINING);
        }
    }

    @Override
    public void reload() {
        this.minAmount = plugin.getConfig().get().getLong("enchants." + id + ".Min-Tokens");
        this.maxAmount = plugin.getConfig().get().getLong("enchants." + id + ".Max-Tokens");
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }
}
