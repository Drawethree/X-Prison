package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class TokenatorEnchant extends UltraPrisonEnchantment {
	private final long maxAmount;
	private final long minAmount;
    private final double chance;


    public TokenatorEnchant(UltraPrisonEnchants instance) {
        super(instance, 14);
		this.minAmount = instance.getConfig().get().getLong("enchants." + id + ".Min-Tokens");
		this.maxAmount = instance.getConfig().get().getLong("enchants." + id + ".Max-Tokens");
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
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
            long randAmount;
			randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
			plugin.getCore().getTokens().getApi().addTokens(e.getPlayer(), randAmount);
        }
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }
}
