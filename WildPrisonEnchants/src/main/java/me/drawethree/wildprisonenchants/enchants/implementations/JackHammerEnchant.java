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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JackHammerEnchant extends WildPrisonEnchantment {
    private final double chance;

    private static CooldownMap<Player> cooldownMap = CooldownMap.create(Cooldown.of(10, TimeUnit.SECONDS));

    public JackHammerEnchant(WildPrisonEnchants instance) {
        super(instance, 10);
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
            if (regions.size() == 1) {
                ProtectedRegion region = regions.get(0);
                int blockCount = 0;
                int timesRan = 0;
                for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                    List<Block> blocksToRemove = new ArrayList<>();
                    for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                        blocksToRemove.add(e.getBlock().getWorld().getBlockAt(x, e.getBlock().getY(), z));
                    }
                    blockCount += blocksToRemove.size();
                    Schedulers.sync().runLater(() -> {
                        blocksToRemove.stream().filter(b -> b != null && b.getType() != Material.AIR).forEach(b -> {
                            b.setType(Material.AIR);
                            WildPrisonEnchants.getEnchantsManager().handleBlockBreak(new BlockBreakEvent(b, e.getPlayer()));
                        });
                    }, timesRan * 1);
                    timesRan++;
                }
                WildPrisonEnchants.getEnchantsManager().addBlocksBroken(e.getPlayer(), blockCount - 1);
                WildPrisonTokens.getInstance().getTokensManager().addBlocksBroken(e.getPlayer(), blockCount - 1);
            }
        }
    }
}
