package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.time.Time;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LayerEnchant extends UltraPrisonEnchantment {

    private double chance;


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

        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {

            long startTime = Time.nowMillis();
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
                for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
                    for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
                        Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
                        if (b1 != null && b1.getType() != CompMaterial.AIR.toMaterial()) {
                            blockCount++;
                            blocksAffected.add(b1);
                            if (plugin.getCore().getAutoSell().isEnabled() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p)) {
                                totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), b1.getType()) + 0.0) * amplifier);
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
                if (plugin.getCore().getAutoSell().isEnabled()) {
                    plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
                }
                plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
                plugin.getCore().getTokens().getTokensManager().addBlocksBroken(null, p, blockCount);
                plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected);

            }
            long timeEnd = Time.nowMillis();
            this.plugin.getCore().debug("LayerEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.");
        }
    }

    @Override
    public void reload() {
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }


}
