package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.nonetaken.wildmines.mines.Mine;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JackHammerEnchant extends WildPrisonEnchantment {


    private final HashMap<UUID, Integer> progress = new HashMap<>();

    public JackHammerEnchant(WildPrisonEnchants instance) {
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
        if (plugin.hasJackHammerDisabled(e.getPlayer())) {
            return;
        }

        int currentProgress = this.progress.getOrDefault(e.getPlayer().getUniqueId(), 0);


        if (currentProgress + 1 >= this.getRequiredBlocks(enchantLevel)) {
            Block b = e.getBlock();
            List<ProtectedRegion> regions = plugin.getCore().getWorldGuard().getRegionContainer().get(b.getWorld()).getApplicableRegions(b.getLocation()).getRegions().stream().filter(reg -> reg.getId().startsWith("mine")).collect(Collectors.toList());
            if (regions.size() > 0) {
                Player p = e.getPlayer();
                ProtectedRegion region = regions.get(0);
                List<Block> blocksAffected = new ArrayList<>();

                double totalDeposit = 0;
                int blockCount = 0;
                int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
                int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                    for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                        Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
                        if (b1 != null && b1.getType() != Material.AIR) {
                            blockCount++;
                            blocksAffected.add(b1);
                            if (plugin.getCore().getAutoSell().hasAutoSellEnabled(p)) {
                                totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region, b1) + 0.0) * amplifier);
                            } else {
                                if (b1.getType() != Material.ENDER_STONE) {
                                    p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
                                }
                            }
                            b1.setType(Material.AIR);
                        }
                    }
                }

                //Mine mine = plugin.getCore().getPrisonMines().getAPI().getByLocation(e.getBlock().getLocation());
                //plugin.getCore().getPrisonMines().getAPI().onBlockBreak(mine, blocksAffected.size());
                //plugin.getCore().getJetsPrisonMines().getAPI().blockBreak(blocksAffected);
                //Bukkit.getPluginManager().callEvent(new JackHammerTriggerEvent(e.getPlayer(), region, blocksAffected));

                Mine mine = plugin.getCore().getWildMinesAPI().getMine(e.getBlock().getLocation());
                if (mine != null) {
                    plugin.getCore().getWildMinesAPI().getMine(e.getBlock().getLocation()).blockBreak(blocksAffected.size());
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
