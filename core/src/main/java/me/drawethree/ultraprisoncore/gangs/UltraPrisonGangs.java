package me.drawethree.ultraprisoncore.gangs;


import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPI;
import me.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPIImpl;
import me.drawethree.ultraprisoncore.gangs.commands.*;
import me.drawethree.ultraprisoncore.gangs.managers.GangsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class UltraPrisonGangs implements UltraPrisonModule {

    public static final String MODULE_NAME = "Gangs";

    public static final String GANGS_ADMIN_PERM = "ultraprison.gangs.admin";

    @Getter
    private static UltraPrisonGangs instance;

    @Getter
    private UltraPrisonGangsAPI api;


    @Getter
    private FileManager.Config config;

    @Getter
    private GangsManager gangsManager;

    @Getter
    private UltraPrisonCore core;

    private Map<String, String> messages;
    private Map<String, GangCommand> commands;

    private boolean enabled;

    public UltraPrisonGangs(UltraPrisonCore prisonCore) {
        instance = this;
        this.core = prisonCore;
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {

        this.config.reload();

        this.loadMessages();

        this.gangsManager.reloadConfig();
    }


    @Override
    public void enable() {

        this.enabled = true;
        this.config = this.core.getFileManager().getConfig("gangs.yml").copyDefaults(true).save();

        this.loadMessages();

        this.gangsManager = new GangsManager(this);

        this.api = new UltraPrisonGangsAPIImpl(this.gangsManager);

        this.registerCommands();
        this.registerEvents();
    }


    @Override
    public void disable() {
        this.gangsManager.saveDataOnDisable();
        this.enabled = false;

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    private void registerEvents() {
    }

    private void registerCommands() {
        this.commands = new HashMap<>();

        registerCommand(new GangHelpCommand(this));
        registerCommand(new GangHelpCommand(this));
        registerCommand(new GangInfoCommand(this));
        registerCommand(new GangCreateCommand(this));
        registerCommand(new GangInviteCommand(this));
        registerCommand(new GangAcceptCommand(this));
        registerCommand(new GangLeaveCommand(this));
        registerCommand(new GangDisbandCommand(this));
        registerCommand(new GangKickCommand(this));
        registerCommand(new GangTopCommand(this));
        registerCommand(new GangAdminCommand(this));
        registerCommand(new GangValueCommand(this));
        //registerCommand(new GangChatCommand(this));

        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.getCommand("help").execute(c.sender(), c.args());
                        //new GangHelpGUI((Player) c.sender()).open();
                        return;
                    }
                    GangCommand subCommand = this.getCommand(Objects.requireNonNull(c.rawArg(0)));
                    if (subCommand != null) {
						if (!subCommand.canExecute(c.sender())) {
							c.sender().sendMessage(this.getMessage("no-permission"));
							return;
						}
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        this.getCommand("help").execute(c.sender(), c.args());
                    }
                }).registerAndBind(core, "gang", "gangs");
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

    private void registerCommand(GangCommand command) {
        this.commands.put(command.getName(), command);

        if (command.getAliases() == null || command.getAliases().length == 0) {
            return;
        }

        for (String alias : command.getAliases()) {
            this.commands.put(alias, command);
        }
    }

    private GangCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}