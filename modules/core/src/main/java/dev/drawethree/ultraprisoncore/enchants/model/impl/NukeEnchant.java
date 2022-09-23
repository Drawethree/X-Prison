package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.api.events.NukeTriggerEvent;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.utils.EnchantUtils;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.utils.Constants;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class NukeEnchant extends UltraPrisonEnchantment {

	private double chance;
	private boolean countBlocksBroken;

	public NukeEnchant(UltraPrisonEnchants instance) {
		super(instance, 21);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Count-Blocks-Broken");
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

			IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

			if (region != null) {
				this.plugin.getCore().debug("Found region: " + region.getId(), this.plugin);
				Player p = e.getPlayer();
				ICuboidSelection selection = (ICuboidSelection) region.getSelection();

				List<Block> blocksAffected = new ArrayList<>();
				long startTimeLoop = Time.nowMillis();
				for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
					for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
						for (int y = selection.getMinimumPoint().getBlockY(); y <= selection.getMaximumPoint().getBlockY(); y++) {
							Block b1 = b.getWorld().getBlockAt(x, y, z);
							if (b1.getType() == Material.AIR) {
								continue;
							}
							blocksAffected.add(b1);
						}
					}
				}

				this.plugin.getCore().debug("NukeEnchant::onBlockBreak::LoopingBlocks >> Took " + (System.currentTimeMillis() - startTimeLoop) + " ms.", this.plugin);

				NukeTriggerEvent event = this.callNukeTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

				if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
					this.plugin.getCore().debug("NukeEnchant::onBlockBreak >> NukeTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
					return;
				}

				double totalDeposit = 0;
				int fortuneLevel = EnchantUtils.getItemFortuneLevel(p.getItemInHand());
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
				boolean autoSellEnabledPlayer = this.plugin.isAutoSellModuleEnabled() && plugin.getCore().getAutoSell().getManager().hasAutoSellEnabled(p);

				for (Block block : blocksAffected) {
					if (autoSellEnabledPlayer) {
						totalDeposit += ((plugin.getCore().getAutoSell().getManager().getPriceForBlock(region.getId(), block) + 0.0) * amplifier);
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

				if (this.plugin.isMinesModuleEnabled()) {
					Mine mine = plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation());

					if (mine != null) {
						mine.handleBlockBreak(blocksAffected);
					}
				}

				this.giveEconomyRewardsToPlayer(p,totalDeposit);

				if (this.countBlocksBroken) {
					plugin.getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
				}

				plugin.getCore().getTokens().getTokensManager().handleBlockBreak(p, blocksAffected, countBlocksBroken);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("NukeEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", this.plugin);
		}

	}

	private void giveEconomyRewardsToPlayer(Player p, double totalDeposit) {
		boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(p);

		double total = this.plugin.isMultipliersModuleEnabled() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;
		total = luckyBooster ? total * 2 : total;

		plugin.getCore().getEconomy().depositPlayer(p, total);

		if (plugin.isAutoSellModuleEnabled()) {
			plugin.getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
		}
	}

	private NukeTriggerEvent callNukeTriggerEvent(Player p, IWrappedRegion region, Block startBlock,List<Block> affectedBlocks) {
		NukeTriggerEvent event = new NukeTriggerEvent(p,region,startBlock,affectedBlocks);
		Events.callSync(event);
		this.plugin.getCore().debug("NukeEnchant::callNukeTriggerEvent >> NukeTriggerEvent called.", this.plugin);
		return event;
	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Count-Blocks-Broken");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
