package dev.drawethree.ultraprisoncore.mines;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.mines.api.UltraPrisonMinesAPI;
import dev.drawethree.ultraprisoncore.mines.api.UltraPrisonMinesAPIImpl;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.commands.impl.*;
import dev.drawethree.ultraprisoncore.mines.listener.MinesListener;
import dev.drawethree.ultraprisoncore.mines.managers.MineManager;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UltraPrisonMines implements UltraPrisonModule {

	public static final String MODULE_NAME = "Mines";

	public static final String MINES_ADMIN_PERM = "ultraprison.mines.admin";

	@Getter
	private static UltraPrisonMines instance;

	private boolean enabled;

	private Map<String, String> messages;
	@Getter
	private FileManager.Config config;
	@Getter
	private MineManager manager;
	@Getter
	private UltraPrisonMinesAPI api;
	@Getter
	private final UltraPrisonCore core;

	private Map<String, MineCommand> commands;


	public UltraPrisonMines(UltraPrisonCore core) {
		instance = this;
		this.core = core;
		this.enabled = false;
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("mines.yml").copyDefaults(true).save();
		this.loadMessages();
		this.manager = new MineManager(this);
		this.manager.enable();
		new MinesListener(this).register();
		this.registerCommands();
		this.api = new UltraPrisonMinesAPIImpl(this);
	}

	@Override
	public void disable() {
		this.enabled = false;
		this.manager.disable();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void reload() {
		this.config.reload();

		this.loadMessages();

		this.manager.reload();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}

	@Override
	public void resetPlayerData() {
	}

	private void loadMessages() {
		this.messages = new HashMap<>();
		for (String key : this.config.get().getConfigurationSection("messages").getKeys(false)) {
			this.messages.put(key.toLowerCase(), TextUtils.applyColor(this.config.get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return this.messages.getOrDefault(key.toLowerCase(), TextUtils.applyColor("&cInvalid message key: " + key));
	}

	private void registerCommands() {
		this.commands = new HashMap<>();

		registerCommand(new MineCreateCommand(this));
		registerCommand(new MineDeleteCommand(this));
		registerCommand(new MineRedefineCommand(this));
		registerCommand(new MinePanelCommand(this));
		registerCommand(new MineTeleportCommand(this));
		registerCommand(new MineToolCommand(this));
		registerCommand(new MineHelpCommand(this));
		registerCommand(new MineResetCommand(this));
		registerCommand(new MineListCommand(this));
		registerCommand(new MineAddBlockCommand(this));
		registerCommand(new MineSetTpCommand(this));
		registerCommand(new MineSaveCommand(this));
		registerCommand(new MineMigrateCommand(this));
		registerCommand(new MineRenameCommand(this));

		Commands.create()
				.handler(c -> {

					if (c.args().size() == 0 && c.sender() instanceof Player) {
						this.getCommand("help").execute(c.sender(), c.args());
						return;
					}

					MineCommand subCommand = this.getCommand(Objects.requireNonNull(c.rawArg(0)));

					if (subCommand != null) {
						if (!subCommand.canExecute(c.sender())) {
							PlayerUtils.sendMessage(c.sender(), this.getMessage("no_permission"));
							return;
						}

						if (!subCommand.execute(c.sender(), c.args().subList(1, c.args().size()))) {
							PlayerUtils.sendMessage(c.sender(), subCommand.getUsage());
						}

					} else {
						this.getCommand("help").execute(c.sender(), c.args());
					}
				}).registerAndBind(core, "mines", "mine");
	}

	private MineCommand getCommand(String name) {
		return this.commands.get(name.toLowerCase());
	}


	private void registerCommand(MineCommand command) {
		this.commands.put(command.getName(), command);

		if (command.getAliases() == null || command.getAliases().length == 0) {
			return;
		}

		for (String alias : command.getAliases()) {
			this.commands.put(alias, command);
		}
	}
}
