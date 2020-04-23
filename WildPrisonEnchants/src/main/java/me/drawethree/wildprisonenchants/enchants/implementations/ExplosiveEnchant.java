package me.drawethree.wildprisonenchants.enchants.implementations;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
    private final int radius1;
    private final int radius2;

    private static CooldownMap<Player> cooldownMap = CooldownMap.create(Cooldown.of(10, TimeUnit.SECONDS));


    public ExplosiveEnchant(WildPrisonEnchants instance) {
        super(instance, 9);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
        this.radius1 = plugin.getConfig().getInt("enchants." + id + ".Radius");
        this.radius2 = plugin.getConfig().getInt("enchants." + id + ".Radius2");

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
            if (regions.size() == 1) {
                ProtectedRegion region = regions.get(0);
                int radius = 2 + (enchantLevel / 100);
                e.getBlock().getWorld().createExplosion(e.getBlock().getLocation().getX(), e.getBlock().getLocation().getY(), e.getBlock().getLocation().getZ(), 0F, false, false);

                int blockCount = 0;
                //int move = (radius / 2 - 1) + (radius % 2 == 0 ? 0 : 1);
                for (int x = e.getBlock().getX(); x > e.getBlock().getX() - radius; x--) {
                    for (int z = e.getBlock().getZ(); z > e.getBlock().getZ() - radius; z--) {
                        for (int y = e.getBlock().getY(); y > e.getBlock().getY() - radius; y--) {
                            Block b = e.getBlock().getWorld().getBlockAt(x, y, z);
                            if (region.contains(x, y, z)) {
                                blockCount++;
                                Schedulers.sync().run(() -> {
                                    b.setType(Material.AIR);
                                    WildPrisonEnchants.getEnchantsManager().handleBlockBreak(new BlockBreakEvent(b, e.getPlayer()));
                                });
                            }
                        }
                    }
                }
                WildPrisonEnchants.getEnchantsManager().addBlocksBroken(e.getPlayer(), blockCount-1);
                WildPrisonTokens.getInstance().getTokensManager().addBlocksBroken(e.getPlayer(), blockCount - 1);
            }
        }
    }
}
