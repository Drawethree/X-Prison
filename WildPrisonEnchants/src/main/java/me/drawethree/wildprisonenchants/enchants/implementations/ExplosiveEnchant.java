package me.drawethree.wildprisonenchants.enchants.implementations;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.drawethree.wildprisonautosell.WildPrisonAutoSell;
import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExplosiveEnchant extends WildPrisonEnchantment {
    private final double chance;

    private static CooldownMap<Player> cooldownMap = CooldownMap.create(Cooldown.of(10, TimeUnit.SECONDS));


    public ExplosiveEnchant(WildPrisonEnchants instance) {
        super(instance, 9);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
    }


    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (!cooldownMap.test(e.getPlayer())) {
            return;
        }
        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
            List<ProtectedRegion> regions = plugin.getWorldGuard().getRegionContainer().get(e.getBlock().getWorld()).getApplicableRegions(e.getBlock().getLocation()).getRegions().stream().filter(reg -> reg.getId().startsWith("mine")).collect(Collectors.toList());
            if (regions.size() > 0) {
                ProtectedRegion region = regions.get(0);
                int radius = 2 + (enchantLevel / 100);

                e.getBlock().getWorld().createExplosion(e.getBlock().getLocation().getX(), e.getBlock().getLocation().getY(), e.getBlock().getLocation().getZ(), 0F, false, false);

                //List<BlockState> blocksAffected = new ArrayList<>();
                //int move = (radius / 2 - 1) + (radius % 2 == 0 ? 0 : 1);
                long totalDeposit = 0;
                int blockCount = 0;
                int fortuneLevel = WildPrisonEnchants.getApi().getEnchantLevel(e.getPlayer(), 3);
                int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                for (int x = e.getBlock().getX(); x > e.getBlock().getX() - radius; x--) {
                    for (int z = e.getBlock().getZ(); z > e.getBlock().getZ() - radius; z--) {
                        for (int y = e.getBlock().getY(); y > e.getBlock().getY() - radius; y--) {
                            Block b = e.getBlock().getWorld().getBlockAt(x, y, z);
                            if (region.contains(x, y, z) && b != null && b.getType() != Material.AIR) {
                                blockCount++;
                                //blocksAffected.add(b.getState());
                                if (WildPrisonAutoSell.getApi().hasAutoSellEnabled(e.getPlayer())) {
                                    totalDeposit += (WildPrisonAutoSell.getApi().getPriceForBrokenBlock(region, b) * amplifier);
                                    System.out.println(totalDeposit);
                                } else {
                                    e.getPlayer().getInventory().addItem(new ItemStack(b.getType(), fortuneLevel + 1));
                                }
                                Schedulers.sync().run(() -> b.setType(Material.AIR));
                            }
                        }
                    }
                }

                //Bukkit.getPluginManager().callEvent(new ExplosionTriggerEvent(e.getPlayer(), region, blocksAffected));
                WildPrisonEnchants.getEconomy().depositPlayer(e.getPlayer(), totalDeposit);
                WildPrisonEnchants.getEnchantsManager().addBlocksBrokenToItem(e.getPlayer(), blockCount);
                WildPrisonTokens.getInstance().getTokensManager().addBlocksBroken(e.getPlayer(), blockCount);
            }
        }
    }
}
