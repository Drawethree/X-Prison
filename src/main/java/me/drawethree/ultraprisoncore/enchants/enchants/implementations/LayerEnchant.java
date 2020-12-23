package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LayerEnchant extends UltraPrisonEnchantment {


    private final HashMap<UUID, Integer> progress = new HashMap<>();

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

        int currentProgress = this.progress.getOrDefault(e.getPlayer().getUniqueId(), 0);


        if (currentProgress + 1 >= this.getRequiredBlocks(enchantLevel)) {
            Block b = e.getBlock();
            List<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(b.getLocation()).stream().filter(reg -> reg.getId().toLowerCase().startsWith("mine")).collect(Collectors.toList());
            if (regions.size() > 0) {
                Player p = e.getPlayer();
                IWrappedRegion region = regions.get(0);
                ICuboidSelection selection = (ICuboidSelection) region.getSelection();

                List<Block> blocksAffected = new ArrayList<>();

                double totalDeposit = 0;
                int blockCount = 0;
                int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
                int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++){
                    for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
                        Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
                        if (b1 != null && b1.getType() != CompMaterial.AIR.toMaterial()) {
                            blockCount++;
                            blocksAffected.add(b1);
                            if (plugin.getCore().getAutoSell().hasAutoSellEnabled(p)) {
                                totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region, b1) + 0.0) * amplifier);
                            } else {
                                p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
                            }
                            b1.setType(CompMaterial.AIR.toMaterial());
                        }
                    }
                }

                if (plugin.getCore().getJetsPrisonMinesAPI() != null) {
                    plugin.getCore().getJetsPrisonMinesAPI().blockBreak(blocksAffected);
                }
                boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());
                double total = luckyBooster ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit) * 2 : plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit);

                plugin.getCore().getEconomy().depositPlayer(p, total);
                plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
                plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
                plugin.getCore().getTokens().getTokensManager().addBlocksBroken(null, p, blockCount);

                this.progress.put(e.getPlayer().getUniqueId(), 0);
            }
        } else {
            this.progress.put(e.getPlayer().getUniqueId(), currentProgress + 1);
        }
    }


    public int getRequiredBlocks(int enchantLevel) {
        if (enchantLevel <= 100) {
            return 900;
        } else if (enchantLevel <= 200) {
            return 750;
        } else if (enchantLevel <= 300) {
            return 600;
        } else if (enchantLevel <= 400) {
            return 450;
        } else if (enchantLevel <= 499) {
            return 300;
        } else {
            return 150;
        }
    }
}
