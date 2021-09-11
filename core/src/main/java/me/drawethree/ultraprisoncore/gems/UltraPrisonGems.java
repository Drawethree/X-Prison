package me.drawethree.ultraprisoncore.gems;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPI;
import me.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPIImpl;
import me.drawethree.ultraprisoncore.gems.commands.*;
import me.drawethree.ultraprisoncore.gems.managers.GemsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public final class UltraPrisonGems implements UltraPrisonModule {

    public static final String MODULE_NAME = "Gems";

    public static final String GEMS_ADMIN_PERM = "ultraprison.gems.admin";

    @Getter
    private static UltraPrisonGems instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private UltraPrisonGemsAPI api;

    @Getter
    private GemsManager gemsManager;
    @Getter
    private UltraPrisonCore core;

    private HashMap<String, String> messages;
    private HashMap<String, GemsCommand> commands;
    private boolean enabled;

    public UltraPrisonGems(UltraPrisonCore UltraPrisonCore) {
        instance = this;
        this.core = UltraPrisonCore;
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.config.reload();
        this.loadMessages();
        this.gemsManager.reload();
    }

    @Override
    public void enable() {
        this.enabled = true;
        this.config = this.core.getFileManager().getConfig("gems.yml").copyDefaults(true).save();
        this.loadMessages();
        this.gemsManager = new GemsManager(this);
        this.api = new UltraPrisonGemsAPIImpl(this.gemsManager);
        this.registerCommands();
        this.registerEvents();
    }


    @Override
    public void disable() {
        this.gemsManager.stopUpdating();
        this.gemsManager.savePlayerDataOnDisable();
        this.enabled = false;

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    private void registerEvents() {
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

        this.commands = new HashMap<>();
        this.commands.put("give", new GemsGiveCommand(this));
        this.commands.put("add", new GemsGiveCommand(this));
        this.commands.put("remove", new GemsRemoveCommand(this));
        this.commands.put("set", new GemsSetCommand(this));
        this.commands.put("help", new GemsHelpCommand(this));
        this.commands.put("pay", new GemsPayCommand(this));

        Commands.create()
                .handler(c -> {

                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.gemsManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
                        return;
                    }

                    GemsCommand subCommand = this.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        if (subCommand.canExecute(c.sender())) {
                            subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                        } else {
                            c.sender().sendMessage(this.getMessage("no_permission"));
                        }
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
                .registerAndBind(core, "gemstop", "gemtop");
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

    private GemsCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
