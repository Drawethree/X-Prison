package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import me.drawethree.ultraprisoncore.utils.RegionUtils;
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
	private int cooldown;


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
				int threshold = this.getMaxLevel() / 3;
				int radius = enchantLevel <= threshold ? 3 : enchantLevel <= threshold * 2 ? 4 : 5;

				b.getWorld().createExplosion(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 0F, false, false);

				List<Block> blocksAffected = new ArrayList<>();
				//int move = (radius / 2 - 1) + (radius % 2 == 0 ? 0 : 1);
				double totalDeposit = 0;
				int blockCount = 0;
				int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;

				final Location startLocation = e.getBlock().getLocation();

				boolean autoSellPlayerEnabled = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				for (int x = startLocation.getBlockX() - (radius == 4 ? 0 : (radius / 2)); x <= startLocation.getBlockX() + (radius == 4 ? radius - 1 : (radius / 2)); x++) {
					for (int z = startLocation.getBlockZ() - (radius == 4 ? 0 : (radius / 2)); z <= startLocation.getBlockZ() + (radius == 4 ? radius - 1 : (radius / 2)); z++) {
						for (int y = startLocation.getBlockY() - (radius == 4 ? 3 : (radius / 2)); y <= startLocation.getBlockY() + (radius == 4 ? 0 : (radius / 2)); y++) {
							Block b1 = b.getWorld().getBlockAt(x, y, z);
							if (!region.contains(b1.getLocation()) || b1.getType() == Material.AIR) {
								continue;
							}
							blockCount++;
							blocksAffected.add(b1);
							if (autoSellPlayerEnabled) {
								totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), b1) + 0.0) * amplifier);
							} else {
								if (this.plugin.getCore().isUltraBackpacksEnabled()) {
									continue;
								}
								p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
							}
						}
					}
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

				boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

				double total = this.plugin.isMultipliersModule() ? luckyBooster ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) * 2 : plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : luckyBooster ? totalDeposit * 2 : totalDeposit;

				plugin.getCore().getEconomy().depositPlayer(p, total);

				if (this.plugin.isAutoSellModule()) {
					plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
				}

				plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
				}

				for (Block b1 : blocksAffected) {
					this.plugin.getCore().getNmsProvider().setBlockInNativeDataPalette(b1.getWorld(), b1.getX(), b1.getY(), b1.getZ(), 0, (byte) 0, true);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> Took " + (timeEnd - timeStart) + " ms.", this.plugin);
		}
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.cooldown = plugin.getConfig().get().getInt("enchants." + id + ".Cooldown");
	}
}
