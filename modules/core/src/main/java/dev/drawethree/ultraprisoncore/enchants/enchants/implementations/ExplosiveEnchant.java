package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.api.events.ExplosionTriggerEvent;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ExplosiveEnchant extends UltraPrisonEnchantment {

	private double chance;
	private boolean countBlocksBroken;
	private boolean soundsEnabled;


	public ExplosiveEnchant(UltraPrisonEnchants instance) {
		super(instance, 9);
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
		if (plugin.hasExplosiveDisabled(e.getPlayer())) {
			return;
		}
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			long timeStart = Time.nowMillis();
			Block b = e.getBlock();
			IWrappedRegion region = RegionUtils.getMineRegionWithHighestPriority(b.getLocation());
			if (region != null) {
				Player p = e.getPlayer();
				int radius = this.calculateRadius(enchantLevel);
				List<Block> blocksAffected = new ArrayList<>();
				final Location startLocation = b.getLocation();

				for (int x = startLocation.getBlockX() - (radius == 4 ? 0 : (radius / 2)); x <= startLocation.getBlockX() + (radius == 4 ? radius - 1 : (radius / 2)); x++) {
					for (int z = startLocation.getBlockZ() - (radius == 4 ? 0 : (radius / 2)); z <= startLocation.getBlockZ() + (radius == 4 ? radius - 1 : (radius / 2)); z++) {
						for (int y = startLocation.getBlockY() - (radius == 4 ? 3 : (radius / 2)); y <= startLocation.getBlockY() + (radius == 4 ? 0 : (radius / 2)); y++) {
							Block b1 = b.getWorld().getBlockAt(x, y, z);
							if (!region.contains(b1.getLocation()) || b1.getType() == Material.AIR) {
								continue;
							}
							blocksAffected.add(b1);
						}
					}
				}

				ExplosionTriggerEvent event = this.callExplosionTriggerEvent(e.getPlayer(), region, blocksAffected);

				if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
					this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> ExplosiveTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
					return;
				}

				blocksAffected = event.getBlocksAffected();

				double totalDeposit = 0;
				int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
				boolean autoSellPlayerEnabled = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				if (this.soundsEnabled) {
					b.getWorld().createExplosion(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 0F, false, false);
				}

				for (Block block : blocksAffected) {
					if (autoSellPlayerEnabled) {
						totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), block) + 0.0) * amplifier);
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

				if (this.plugin.isMinesModule()) {
					Mine mine = plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation());
					if (mine != null) {
						mine.handleBlockBreak(blocksAffected);
					}
				}

				this.giveEconomyRewardToPlayer(p, totalDeposit);

				if (this.countBlocksBroken) {
					plugin.getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
				}

				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected, countBlocksBroken);

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

		double total = this.plugin.isMultipliersModule() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;
		total = luckyBooster ? total * 2 : total;

		plugin.getCore().getEconomy().depositPlayer(p, total);

		if (this.plugin.isAutoSellModule()) {
			plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
		}
	}

	private int calculateRadius(int enchantLevel) {
		int threshold = this.getMaxLevel() / 3;
		return enchantLevel <= threshold ? 3 : enchantLevel <= threshold * 2 ? 4 : 5;
	}

	private ExplosionTriggerEvent callExplosionTriggerEvent(Player p, IWrappedRegion mineRegion, List<Block> blocks) {
		ExplosionTriggerEvent event = new ExplosionTriggerEvent(p, mineRegion, blocks);
		Events.callSync(event);
		return event;
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getConfig().get().getBoolean("enchants." + id + ".Count-Blocks-Broken");
		this.soundsEnabled = plugin.getConfig().get().getBoolean("enchants." + id + ".Sounds");

	}
}
