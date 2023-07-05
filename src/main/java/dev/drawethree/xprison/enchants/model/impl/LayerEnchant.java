package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.api.events.LayerTriggerEvent;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.utils.Constants;
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
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LayerEnchant extends XPrisonEnchantment {

    private double chance;
    private boolean countBlocksBroken;

    public LayerEnchant(XPrisonEnchants instance) {
        super(instance, 10);
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
        double chance = getChanceToTrigger(enchantLevel);

        if (chance < ThreadLocalRandom.current().nextDouble(100)) {
            return;
        }

        long startTime = Time.nowMillis();
        final Player p = e.getPlayer();
        final Block b = e.getBlock();

        IWrappedRegion region = RegionUtils.getRegionWithHighestPriorityAndFlag(b.getLocation(), Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.ALLOW);

        if (region == null) {
            return;
        }

        this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> WG Region used: " + region.getId(), this.plugin);
        List<Block> blocksAffected = this.getAffectedBlocks(b, region);

        LayerTriggerEvent event = this.callLayerTriggerEvent(e.getPlayer(), region, e.getBlock(), blocksAffected);

        if (event.isCancelled() || event.getBlocksAffected().isEmpty()) {
            this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> LayerTriggerEvent was cancelled. (Blocks affected size: " + event.getBlocksAffected().size(), this.plugin);
            return;
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
        this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", this.plugin);
    }

    private List<Block> getAffectedBlocks(Block startBlock, IWrappedRegion region) {
        List<Block> blocksAffected = new ArrayList<>();
        ICuboidSelection selection = (ICuboidSelection) region.getSelection();
        for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
            for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
                Block b1 = startBlock.getWorld().getBlockAt(x, startBlock.getY(), z);
                if (b1.getType() == Material.AIR) {
                    continue;
                }
                blocksAffected.add(b1);
            }
        }
        return blocksAffected;
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

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    private void giveEconomyRewardToPlayer(Player p, double totalDeposit) {
        double total = this.plugin.isMultipliersModuleEnabled() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;

        plugin.getCore().getEconomy().depositPlayer(p, total);

        if (plugin.isAutoSellModuleEnabled()) {
            plugin.getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
        }
    }

    private LayerTriggerEvent callLayerTriggerEvent(Player player, IWrappedRegion region, Block originBlock, List<Block> blocksAffected) {
        LayerTriggerEvent event = new LayerTriggerEvent(player, region, originBlock, blocksAffected);
        Events.callSync(event);
        this.plugin.getCore().debug("LayerEnchant::callLayerTriggerEvent >> LayerTriggerEvent called.", this.plugin);
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
