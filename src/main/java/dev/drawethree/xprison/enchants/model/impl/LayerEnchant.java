package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonObject;
import dev.drawethree.xprison.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.misc.RegionUtils;
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

public final class LayerEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private boolean countBlocksBroken;

    public LayerEnchant() {
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

        getCore().debug("LayerEnchant::onBlockBreak >> WG Region used: " + region.getId(), getEnchants());
        List<Block> blocksAffected = this.getAffectedBlocks(b, region);

        handleAffectedBlocks(p, blocksAffected);

        if (getEnchants().isMinesModuleEnabled()) {
            MineImpl mineImpl = getCore().getMines().getManager().getMineAtLocation(e.getBlock().getLocation());
            if (mineImpl != null) {
                mineImpl.handleBlockBreak(blocksAffected);
            }
        }

        if (this.countBlocksBroken) {
            getEnchants().getEnchantsManager().addBlocksBrokenToItem(p, blocksAffected.size());
        }


        getCore().getTokens().getTokensManager().handleBlockBreak(p, blocksAffected);
        getCore().getBlocks().getBlocksManager().handleBlockBreak(p, blocksAffected, countBlocksBroken);


        long timeEnd = Time.nowMillis();
        getCore().debug("LayerEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", getEnchants());
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

    private void handleAffectedBlocks(Player p, List<Block> blocksAffected) {
        double totalDeposit = 0.0;
        int fortuneLevel = EnchantUtils.getItemFortuneLevel(p.getItemInHand());
        boolean autoSellPlayerEnabled = getEnchants().isAutoSellModuleEnabled() && getCore().getAutoSell().getManager().hasAutoSellEnabled(p);

        for (Block block : blocksAffected) {

            int amplifier = fortuneLevel;
            if (FortuneEnchant.isBlockBlacklisted(block)) {
                amplifier = 1;
            }

            if (autoSellPlayerEnabled) {
                totalDeposit += ((getCore().getAutoSell().getManager().getPriceForBlock(block) + 0.0) * amplifier);
            } else {
                ItemStack itemToGive = XMaterial.matchXMaterial(block.getType()).parseItem();
                itemToGive.setAmount(amplifier);
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
        double total = totalDeposit;

        getCore().getEconomy().depositPlayer(p, total);

        if (getEnchants().isAutoSellModuleEnabled()) {
            getCore().getAutoSell().getManager().addToCurrentEarnings(p, total);
        }
    }


    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.countBlocksBroken = JsonUtils.getBoolean(config,"countBlocksBroken",false);
    }
}
