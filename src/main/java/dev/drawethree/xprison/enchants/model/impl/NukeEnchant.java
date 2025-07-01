package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonObject;
import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.xprison.api.enchants.events.NukeTriggerEvent;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.multipliers.model.MultiplierType;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.misc.MathUtils;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
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
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NukeEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private boolean countBlocksBroken;
    private boolean removeBlocks;
    private boolean useEvents;
    private String message;

    public NukeEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        long startTime = Time.nowMillis();
        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

        if (region == null) {
            return;
        }

        getCore().debug("NukeEnchant::onBlockBreak >> WG Region used: " + region.getId(), getEnchants());
        List<Block> blocksAffected = this.getAffectedBlocks(b, region);

        NukeTriggerEvent event = this.callNukeTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

        if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
            getCore().debug("NukeEnchant::onBlockBreak >> NukeTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), getEnchants());
            return;
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

        if (!this.useEvents && getEnchants().isMinesModuleEnabled() && removeBlocks) {
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
        getCore().debug("NukeEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", getEnchants());
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

            if (this.removeBlocks) {
                block.setType(Material.AIR, true);
            }

        }
        this.giveEconomyRewardsToPlayer(p, totalDeposit);
    }

    private List<Block> getAffectedBlocks(Block b, IWrappedRegion region) {
        List<Block> blocksAffected = new ArrayList<>();
        ICuboidSelection selection = (ICuboidSelection) region.getSelection();
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
        return blocksAffected;
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    private void giveEconomyRewardsToPlayer(Player p, double totalDeposit) {
        double total = getEnchants().isMultipliersModuleEnabled() ? getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;

        getCore().getEconomy().depositPlayer(p, total);

        if (getEnchants().isAutoSellModuleEnabled()) {
            getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
        }

        if (message != null || !message.isEmpty()) {
            PlayerUtils.sendMessage(p,message.replace("%money%", MathUtils.formatNumber(total)));
        }
    }

    private NukeTriggerEvent callNukeTriggerEvent(Player p, IWrappedRegion region, Block startBlock, List<Block> affectedBlocks) {
        NukeTriggerEvent event = new NukeTriggerEvent(p, region, startBlock, affectedBlocks);
        Events.callSync(event);
        getCore().debug("NukeEnchant::callNukeTriggerEvent >> NukeTriggerEvent called.", getEnchants());
        return event;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.countBlocksBroken = JsonUtils.getBoolean(config,"countBlocksBroken", false);
        this.removeBlocks = JsonUtils.getBoolean(config,"removeBlocks", true);
        this.useEvents = JsonUtils.getBoolean(config,"useEvents", false);
        this.message = TextUtils.applyColor(JsonUtils.getString(config,"message",""));
    }
}
