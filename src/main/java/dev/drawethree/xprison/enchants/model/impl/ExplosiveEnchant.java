package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.api.events.ExplosionTriggerEvent;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.block.CuboidExplosionBlockProvider;
import dev.drawethree.xprison.utils.block.ExplosionBlockProvider;
import dev.drawethree.xprison.utils.block.SpheroidExplosionBlockProvider;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.misc.RegionUtils;
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
import java.util.stream.Collectors;

public final class ExplosiveEnchant extends XPrisonEnchantment {

    private double chance;
    private boolean countBlocksBroken;
    private boolean soundsEnabled;
    private ExplosionBlockProvider blockProvider;

    public ExplosiveEnchant(XPrisonEnchants instance) {
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
        double chance = getChanceToTrigger(enchantLevel);

        if (chance < ThreadLocalRandom.current().nextDouble(100)) {
            return;
        }

        long timeStart = Time.nowMillis();
        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

        if (region == null) {
            return;
        }

        this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> WG Region used: " + region.getId(), this.plugin);
        int radius = this.calculateRadius(enchantLevel);

        List<Block> blocksAffected = this.blockProvider.provide(b, radius).stream().filter(block -> region.contains(block.getLocation()) && block.getType() != Material.AIR).collect(Collectors.toList());

        ExplosionTriggerEvent event = this.callExplosionTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

        if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
            this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> ExplosiveTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
            return;
        }

        if (this.soundsEnabled) {
            b.getWorld().createExplosion(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 0F, false, false);
        }

        blocksAffected = event.getBlocksAffected();

        if (!this.plugin.getCore().isUltraBackpacksEnabled()) {
            handleAffectedBlocks(p, region, blocksAffected);
        } else {
            UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
        }

        if (this.plugin.isMinesModuleEnabled()) {
            Mine mine = plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation());
            if (mine != null) {
                mine.handleBlockBreak(blocksAffected);
            }
        }

        if (this.countBlocksBroken) {
            plugin.getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
        }

        plugin.getCore().getTokens().getTokensManager().handleBlockBreak(p, blocksAffected, countBlocksBroken);

        long timeEnd = Time.nowMillis();
        this.plugin.getCore().debug("ExplosiveEnchant::onBlockBreak >> Took " + (timeEnd - timeStart) + " ms.", this.plugin);
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    private void handleAffectedBlocks(Player p, IWrappedRegion region, List<Block> blocksAffected) {
        double totalDeposit = 0.0;
        int fortuneLevel = EnchantUtils.getItemFortuneLevel(p.getItemInHand());
        boolean autoSellPlayerEnabled = this.plugin.isAutoSellModuleEnabled() && plugin.getCore().getAutoSell().getManager().hasAutoSellEnabled(p);

        for (Block block : blocksAffected) {

            int amplifier = fortuneLevel;

            if (FortuneEnchant.isBlockBlacklisted(block)) {
                amplifier = 1;
            }

            if (autoSellPlayerEnabled) {
                totalDeposit += ((plugin.getCore().getAutoSell().getManager().getPriceForBlock(region.getId(), block) + 0.0) * amplifier);
            } else {
                ItemStack itemToGive = CompMaterial.fromBlock(block).toItem(amplifier);
                p.getInventory().addItem(itemToGive);
            }
            block.setType(Material.AIR, true);
        }
        this.giveEconomyRewardToPlayer(p, totalDeposit);
    }

    private void giveEconomyRewardToPlayer(Player p, double totalDeposit) {

        double total = this.plugin.isMultipliersModuleEnabled() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;

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
