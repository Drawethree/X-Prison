package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultrabackpacks.UltraBackpacks;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
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

public class NukeEnchant extends UltraPrisonEnchantment {

	private double chance;

	public NukeEnchant(UltraPrisonEnchants instance) {
		super(instance, 21);
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

				boolean autoSellEnabledPlayer = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
					for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
						for (int y = selection.getMinimumPoint().getBlockY(); y <= selection.getMaximumPoint().getBlockY(); y++) {
							Block b1 = b.getWorld().getBlockAt(x, y, z);
							if (b1 == null || b1.getType() == Material.AIR) {
								continue;
							}
							blockCount++;
							blocksAffected.add(b1);
							if (autoSellEnabledPlayer) {
								totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), b1.getType()) + 0.0) * amplifier);
							} else {
								if (plugin.getCore().isUltraBackpacksEnabled()) {
									continue;
								}
								p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
							}
						}
					}
				}

				this.plugin.getCore().debug("NukeEnchant::onBlockBreak::LoopingBlocks >> Took " + (System.currentTimeMillis() - startTime) + " ms.");

				if (plugin.getCore().getJetsPrisonMinesAPI() != null) {
					plugin.getCore().getJetsPrisonMinesAPI().blockBreak(blocksAffected);
				}

				boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());
				double total = this.plugin.isMultipliersModule() ? luckyBooster ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) * 2 : plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : luckyBooster ? totalDeposit * 2 : totalDeposit;

				plugin.getCore().getEconomy().depositPlayer(p, total);

				if (this.plugin.isAutoSellModule()) {
					plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
				}

				plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacks.getInstance().getApi().handleBlocksBroken(p, blocksAffected);
				}

				for (Block b1 : blocksAffected) {
					this.plugin.getCore().getNmsProvider().setBlockInNativeDataPalette(b1.getWorld(), b1.getX(), b1.getY(), b1.getZ(), 0, (byte) 0, true);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("NukeEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.");
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
