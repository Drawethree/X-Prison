package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonObject;
import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.xprison.api.enchants.events.ExplosionTriggerEvent;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.multipliers.model.MultiplierType;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.block.ExplosionBlockProvider;
import dev.drawethree.xprison.utils.block.ExplosionType;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;
import java.util.stream.Collectors;

public final class ExplosiveEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private boolean countBlocksBroken;
    private boolean soundsEnabled;
    private boolean useEvents;
    private ExplosionBlockProvider blockProvider;

    public ExplosiveEnchant() {
    }



    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        long timeStart = Time.nowMillis();
        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

        if (region == null) {
            return;
        }

        getCore().debug("ExplosiveEnchant::onBlockBreak >> WG Region used: " + region.getId(), getEnchants());
        int radius = this.calculateRadius(enchantLevel);

        List<Block> blocksAffected = this.blockProvider.provide(b, radius).stream().filter(block -> region.contains(block.getLocation()) && block.getType() != Material.AIR).collect(Collectors.toList());

        ExplosionTriggerEvent event = this.callExplosionTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

        if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
            getCore().debug("ExplosiveEnchant::onBlockBreak >> ExplosiveTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), getEnchants());
            return;
        }

        if (this.soundsEnabled) {
            b.getWorld().createExplosion(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 0F, false, false);
        }

        if (this.useEvents) {
            final List<BlockBreakEvent> ignored = getEnchants().getEnchantsListener().getIgnoredEvents();
            blocksAffected = event.getBlocksAffected().stream().filter(block -> {
                final BlockBreakEvent blockEvent = new BlockBreakEvent(block, p);
                ignored.add(blockEvent);
                Bukkit.getPluginManager().callEvent(blockEvent);
                ignored.remove(blockEvent);
                return !e.isCancelled();
            }).collect(Collectors.toList());
        } else {
            blocksAffected = event.getBlocksAffected();
        }

        if (!getCore().isUltraBackpacksEnabled()) {
            handleAffectedBlocks(p, region, blocksAffected);
        } else {
            UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
        }

        if (!this.useEvents && getEnchants().isMinesModuleEnabled()) {
            MineImpl mineImpl = getCore().getMines().getManager().getMineAtLocation(e.getBlock().getLocation());
            if (mineImpl != null) {
                mineImpl.handleBlockBreak(blocksAffected);
            }
        }

        if (this.countBlocksBroken) {
            getEnchants().getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
        }

        if (!this.useEvents) {
            getCore().getTokens().getTokensManager().handleBlockBreak(p, blocksAffected);
            getCore().getBlocks().getBlocksManager().handleBlockBreak(p, blocksAffected, countBlocksBroken);
        }

        long timeEnd = Time.nowMillis();
        getCore().debug("ExplosiveEnchant::onBlockBreak >> Took " + (timeEnd - timeStart) + " ms.", getEnchants());
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    private void handleAffectedBlocks(Player p, IWrappedRegion region, List<Block> blocksAffected) {
        double totalDeposit = 0.0;
        int fortuneLevel = EnchantUtils.getItemFortuneLevel(p.getItemInHand());
        boolean autoSellPlayerEnabled = getEnchants().isAutoSellModuleEnabled() && getCore().getAutoSell().getManager().hasAutoSellEnabled(p);

        for (Block block : blocksAffected) {

            int amplifier = fortuneLevel;

            if (FortuneEnchant.isBlockBlacklisted(block)) {
                amplifier = 1;
            }

            if (autoSellPlayerEnabled) {
                totalDeposit += ((getCore().getAutoSell().getManager().getPriceForBlock(region.getId(), block) + 0.0) * amplifier);
            } else {
                ItemStack itemToGive = XMaterial.matchXMaterial(block.getType()).parseItem();
                itemToGive.setAmount(amplifier);
                p.getInventory().addItem(itemToGive);
            }
            block.setType(Material.AIR, true);
        }
        this.giveEconomyRewardToPlayer(p, totalDeposit);
    }

    private void giveEconomyRewardToPlayer(Player p, double totalDeposit) {

        double total = getEnchants().isMultipliersModuleEnabled() ? getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;

        getCore().getEconomy().depositPlayer(p, total);

        if (getEnchants().isAutoSellModuleEnabled()) {
            getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
        }
    }

    private int calculateRadius(int enchantLevel) {
        int threshold = this.getMaxLevel() / 3;
        return enchantLevel <= threshold ? 3 : enchantLevel <= threshold * 2 ? 4 : 5;
    }

    private ExplosionTriggerEvent callExplosionTriggerEvent(Player p, IWrappedRegion mineRegion, Block originBlock, List<Block> blocks) {
        ExplosionTriggerEvent event = new ExplosionTriggerEvent(p, mineRegion, originBlock, blocks);
        Events.callSync(event);
        getCore().debug("ExplosiveEnchant::callExplosiveTriggerEvent >> ExplosiveTriggerEvent called.", getEnchants());
        return event;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.countBlocksBroken = JsonUtils.getBoolean(config,"countBlocksBroken", false);
        this.soundsEnabled = JsonUtils.getBoolean(config, "sounds", false);
        this.useEvents = JsonUtils.getBoolean(config,"useEvents",false);
        this.blockProvider = ExplosionType.getBlockProvider(ExplosionType.valueOf(JsonUtils.getString(config,"explosionType","CUBE")));
    }
}
