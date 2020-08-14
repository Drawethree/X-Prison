package me.drawethree.wildprisoncore.gems;


import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.gems.api.WildPrisonGemsAPI;
import me.drawethree.wildprisoncore.gems.api.WildPrisonGemsAPIImpl;
import me.drawethree.wildprisoncore.gems.commands.GemsCommand;
import me.drawethree.wildprisoncore.gems.managers.GemsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class WildPrisonGems {

    public static final String GEMS_ADMIN_PERM = "wildprison.gems.admin";

    @Getter
    private static WildPrisonGems instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private WildPrisonGemsAPI api;

    @Getter
    private GemsManager gemsManager;
    @Getter
    private WildPrisonCore core;

    private HashMap<String, String> messages;

    public WildPrisonGems(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("gems.yml").copyDefaults(true).save();
        this.loadMessages();
        this.gemsManager = new GemsManager(this);
        this.api = new WildPrisonGemsAPIImpl(this.gemsManager);
    }


    public void enable() {
        this.registerCommands();
        this.registerEvents();
    }


    public void disable() {
        this.gemsManager.stopUpdating();
        this.gemsManager.savePlayerDataOnDisable();
    }

    private void registerEvents() {
        //TODO: add support ?
        /*Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.DOUBLE_PLANT && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        this.gemsManager.redeemGems(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                })
                .bindWith(core);*/
    }

    private void registerCommands() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.gemsManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
                        return;
                    }
                    GemsCommand subCommand = GemsCommand.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.gemsManager.sendInfoMessage(c.sender(), target);
                    }
                })
                .registerAndBind(core, "gems");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.gemsManager.sendGemsTop(c.sender());
                    }
                })
                .registerAndBind(core, "gemstop");
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(this.getConfig().get().getString("messages." + key)));
        }
    }

    public String getMessage(String key) {
        return messages.get(key);
    }
}
