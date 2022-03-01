package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.api.events.LayerTriggerEvent;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LayerEnchant extends UltraPrisonEnchantment {

	private double chance;
	private boolean countBlocksBroken;

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
			IWrappedRegion region = RegionUtils.getMineRegionWithHighestPriority(b.getLocation());
			if (region != null) {

				Player p = e.getPlayer();
				ICuboidSelection selection = (ICuboidSelection) region.getSelection();
				List<Block> blocksAffected = new ArrayList<>();

				boolean autoSellPlayerEnabled = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
					for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
						Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
						if (b1.getType() == Material.AIR) {
							continue;
						}
						blocksAffected.add(b1);
					}
				}

				LayerTriggerEvent event = this.callLayerTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

				if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
					this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> LayerTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
					return;
				}

				double totalDeposit = 0;
				int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;

				blocksAffected = event.getBlocksAffected();

				for (Block block : blocksAffected) {
					if (autoSellPlayerEnabled) {
						totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), block) + 0.0) * amplifier);
					} else {
						if (plugin.getCore().isUltraBackpacksEnabled()) {
							continue;
						}
						ItemStack toGive = CompMaterial.fromBlock(block).toItem(fortuneLevel + 1);
						p.getInventory().addItem(toGive);
					}
					this.plugin.getCore().getNmsProvider().setBlockInNativeDataPalette(block.getWorld(), block.getX(), block.getY(), block.getZ(), 0, (byte) 0, true);
				}

				if (plugin.getCore().getJetsPrisonMinesAPI() != null) {
					plugin.getCore().getJetsPrisonMinesAPI().blockBreak(blocksAffected);
				}

				if (this.plugin.isMinesModule()) {
					Mine mine = plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation());
					if (mine != null) {
						mine.handleBlockBreak(blocksAffected);
					}
				}

				this.giveEconomyRewardsToPlayer(p, totalDeposit);

				if (this.countBlocksBroken) {
					plugin.getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
				}
				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected, countBlocksBroken);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", this.plugin);
		}
	}

	private void giveEconomyRewardsToPlayer(Player p, double totalDeposit) {
		boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(p);

		double total = this.plugin.isMultipliersModule() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;
		total = luckyBooster ? total * 2 : total;

		plugin.getCore().getEconomy().depositPlayer(p, total);

		if (plugin.isAutoSellModule()) {
			plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
		}
	}

	private LayerTriggerEvent callLayerTriggerEvent(Player player, IWrappedRegion region, Block originBlock, List<Block> blocksAffected) {
		LayerTriggerEvent event = new LayerTriggerEvent(player, region, originBlock, blocksAffected);
		Events.callSync(event);
		return event;
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getConfig().get().getBoolean("enchants." + id + ".Count-Blocks-Broken");

	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}


}
