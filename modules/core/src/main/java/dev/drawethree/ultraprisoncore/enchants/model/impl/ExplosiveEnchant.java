package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.api.events.ExplosionTriggerEvent;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.utils.EnchantUtils;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.utils.Constants;
import dev.drawethree.ultraprisoncore.utils.block.CuboidExplosionBlockProvider;
import dev.drawethree.ultraprisoncore.utils.block.ExplosionBlockProvider;
import dev.drawethree.ultraprisoncore.utils.block.SpheroidExplosionBlockProvider;
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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ExplosiveEnchant extends UltraPrisonEnchantment {

	private double chance;
	private boolean countBlocksBroken;
	private boolean soundsEnabled;
	private ExplosionBlockProvider blockProvider;

	public ExplosiveEnchant(UltraPrisonEnchants instance) {
		super(instance, 9);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Count-Blocks-Broken");
		this.soundsEnabled = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Sounds");
		this.blockProvider = this.loadBlockProvider();
	}

	private ExplosionBlockProvider loadBlockProvider() {
		String explosionType = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Explosion-Type", "CUBE");

		if ("CUBE".equalsIgnoreCase(explosionType)) {
			return CuboidExplosionBlockProvider.instance();
		} else if ("SPHERE".equalsIgnoreCase(explosionType)) {
			return SpheroidExplosionBlockProvider.instance();
		} else {
			return CuboidExplosionBlockProvider.instance();
		}
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
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

			long timeStart = Time.nowMillis();
			Block b = e.getBlock();

			IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

			if (region != null) {
				this.plugin.getCore().debug("Found region: " + region.getId(), this.plugin);
				this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> WG Region used: " + region.getId(), this.plugin);
				Player p = e.getPlayer();
				int radius = this.calculateRadius(enchantLevel);

				List<Block> blocksAffected = this.blockProvider.provide(b, radius);
				blocksAffected.removeIf(block -> !region.contains(block.getLocation()) || block.getType() == Material.AIR);

				ExplosionTriggerEvent event = this.callExplosionTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

				if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
					this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> ExplosiveTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
					return;
				}

				blocksAffected = event.getBlocksAffected();

				double totalDeposit = 0;
				int fortuneLevel = EnchantUtils.getItemFortuneLevel(p.getItemInHand());
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
				boolean autoSellPlayerEnabled = this.plugin.isAutoSellModuleEnabled() && plugin.getCore().getAutoSell().getManager().hasAutoSellEnabled(p);

				if (this.soundsEnabled) {
					b.getWorld().createExplosion(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 0F, false, false);
				}

				for (Block block : blocksAffected) {
					if (autoSellPlayerEnabled) {
						totalDeposit += ((plugin.getCore().getAutoSell().getManager().getPriceForBlock(region.getId(), block) + 0.0) * amplifier);
					} else {
						if (this.plugin.getCore().isUltraBackpacksEnabled()) {
							continue;
						}
						ItemStack itemToGive = CompMaterial.fromBlock(block).toItem(fortuneLevel + 1);
						p.getInventory().addItem(itemToGive);
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

				this.giveEconomyRewardToPlayer(p, totalDeposit);

				if (this.countBlocksBroken) {
					plugin.getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
				}

				plugin.getCore().getTokens().getTokensManager().handleBlockBreak(p, blocksAffected, countBlocksBroken);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> Took " + (timeEnd - timeStart) + " ms.", this.plugin);
		}
	}

	private void giveEconomyRewardToPlayer(Player p, double totalDeposit) {
		boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(p);

		double total = this.plugin.isMultipliersModuleEnabled() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;
		total = luckyBooster ? total * 2 : total;

		plugin.getCore().getEconomy().depositPlayer(p, total);

		if (this.plugin.isAutoSellModuleEnabled()) {
			plugin.getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
		}
	}

	private int calculateRadius(int enchantLevel) {
		int threshold = this.getMaxLevel() / 3;
		return enchantLevel <= threshold ? 3 : enchantLevel <= threshold * 2 ? 4 : 5;
	}

	private ExplosionTriggerEvent callExplosionTriggerEvent(Player p, IWrappedRegion mineRegion, Block originBlock, List<Block> blocks) {
		ExplosionTriggerEvent event = new ExplosionTriggerEvent(p, mineRegion, originBlock, blocks);
		Events.callSync(event);
		this.plugin.getCore().debug("ExplosiveEnchant::callExplosiveTriggerEvent >> ExplosiveTriggerEvent called.", this.plugin);
		return event;
	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Count-Blocks-Broken");
		this.soundsEnabled = plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Sounds");
		this.blockProvider = this.loadBlockProvider();
	}
}
