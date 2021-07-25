package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.lucko.helper.time.Time;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LayerEnchant extends UltraPrisonEnchantment {
	private double chance;


	public LayerEnchant(UltraPrisonEnchants instance) {
		super(instance, 10);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (plugin.hasLayerDisabled(e.getPlayer())) {
			return;
		}

		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {

			long startTime = Time.nowMillis();
			Block b = e.getBlock();
			List<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(b.getLocation()).stream().filter(reg -> reg.getId().toLowerCase().startsWith("mine")).collect(Collectors.toList());
			if (regions.size() > 0) {
				Player p = e.getPlayer();
				IWrappedRegion region = regions.get(0);
				ICuboidSelection selection = (ICuboidSelection) region.getSelection();

				List<Block> blocksAffected = new ArrayList<>();

				double totalDeposit = 0;
				int blockCount = 0;
				int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;

				boolean autoSellPlayerEnabled = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
					for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
						Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
						if (b1 == null || b1.getType() == Material.AIR) {
							continue;
						}
						blockCount++;
						blocksAffected.add(b1);
						if (autoSellPlayerEnabled) {
							totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), b1.getType()) + 0.0) * amplifier);
						} else {
							p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
						}
						this.plugin.getCore().getNmsProvider().setBlockInNativeDataPalette(b1.getWorld(), b1.getX(), b1.getY(), b1.getZ(), 0, (byte) 0, true);
					}
				}

				if (plugin.getCore().getJetsPrisonMinesAPI() != null) {
					plugin.getCore().getJetsPrisonMinesAPI().blockBreak(blocksAffected);
				}

				boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());
				double total = this.plugin.isMultipliersModule() ? luckyBooster ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit) * 2 : plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit) : luckyBooster ? totalDeposit * 2 : totalDeposit;

				plugin.getCore().getEconomy().depositPlayer(p, total);

				if (plugin.isAutoSellModule()) {
					plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
				}

				plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
				plugin.getCore().getTokens().getTokensManager().addBlocksBroken(null, p, blockCount);
				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected);

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.");
		}

	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}


}
