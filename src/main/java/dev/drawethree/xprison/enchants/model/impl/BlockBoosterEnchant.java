package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.tokens.events.XPrisonBlockBreakEvent;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class BlockBoosterEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private static final Map<UUID, Long> BOOSTED_PLAYERS = new HashMap<>();
    private double chance;

    public BlockBoosterEnchant() {
        Events.subscribe(XPrisonBlockBreakEvent.class)
                .handler(e -> {
                    if (BOOSTED_PLAYERS.containsKey(e.getPlayer().getUniqueId())) {
                        List<Block> blocks = new ArrayList<>();
                        for (Block b : e.getBlocks()) {
                            blocks.add(b);
                            blocks.add(b);
                        }
                        e.setBlocks(blocks);
                    }
                }).bindWith(getCore());
    }

    public static boolean hasBlockBoosterRunning(Player p) {
        return BOOSTED_PLAYERS.containsKey(p.getUniqueId());
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (hasBlockBoosterRunning(e.getPlayer())) {
            return;
        }

        PlayerUtils.sendMessage(e.getPlayer(), getEnchants().getEnchantsConfig().getMessage("block_booster_on"));

        BOOSTED_PLAYERS.put(e.getPlayer().getUniqueId(), Time.nowMillis() + TimeUnit.MINUTES.toMillis(1));

        Schedulers.sync().runLater(() -> {
            if (e.getPlayer().isOnline()) {
                PlayerUtils.sendMessage(e.getPlayer(), getEnchants().getEnchantsConfig().getMessage("block_booster_off"));
            }
            BOOSTED_PLAYERS.remove(e.getPlayer().getUniqueId());
        }, 5, TimeUnit.MINUTES);

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
    }
}
