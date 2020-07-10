package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import net.lightshard.prisonmines.mine.Mine;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JackHammerEnchant extends WildPrisonEnchantment {

    private final double chance;
    private final int cooldown;
    private final CooldownMap<Player> cooldownMap;

    public JackHammerEnchant(WildPrisonEnchants instance) {
        super(instance, 10);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.cooldown = plugin.getConfig().get().getInt("enchants." + id + ".Cooldown");
        this.cooldownMap = CooldownMap.create(Cooldown.of(cooldown, TimeUnit.SECONDS));
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
        if (!cooldownMap.test(e.getPlayer())) {
            return;
        }

        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
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

                Mine mine = plugin.getCore().getPrisonMines().getAPI().getByLocation(e.getBlock().getLocation());
                plugin.getCore().getPrisonMines().getAPI().onBlockBreak(mine, blocksAffected.size());
                //plugin.getCore().getJetsPrisonMines().getAPI().blockBreak(blocksAffected);
                //Bukkit.getPluginManager().callEvent(new JackHammerTriggerEvent(e.getPlayer(), region, blocksAffected));

                plugin.getCore().getEconomy().depositPlayer(p, plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit));
                plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
                plugin.getCore().getTokens().getTokensManager().addBlocksBroken(p, blockCount);
            }
        }
    }
}
