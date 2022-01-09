package me.drawethree.ultraprisoncore.mines;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.mines.api.UltraPrisonMinesAPI;
import me.drawethree.ultraprisoncore.mines.api.UltraPrisonMinesAPIImpl;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.commands.impl.*;
import me.drawethree.ultraprisoncore.mines.managers.MineManager;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
	private UltraPrisonCore core;

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
		this.registerCommands();
		this.subscribeEvents();
		this.manager = new MineManager(this);
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
	public String[] getTables() {
		return new String[0];
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		return new String[0];
	}

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}

	private void loadMessages() {
		this.messages = new HashMap<>();
		for (String key : this.config.get().getConfigurationSection("messages").getKeys(false)) {
			this.messages.put(key.toLowerCase(), Text.colorize(this.config.get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return this.messages.getOrDefault(key.toLowerCase(), Text.colorize("&cInvalid message key: " + key));
	}

	private void registerCommands() {
		this.commands = new HashMap<>();

		registerCommand(new MineCreateCommand(this));
		registerCommand(new MineDeleteCommand(this));
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
							PlayerUtils.sendMessage(c.sender(), Text.colorize(subCommand.getUsage()));
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

	private void subscribeEvents() {
		Events.subscribe(PlayerInteractEvent.class)
				.filter(e -> e.getItem() != null && e.getItem().isSimilar(MineManager.SELECTION_TOOL) && e.getClickedBlock() != null)
				.handler(e -> {
					int pos = e.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : e.getAction() == Action.RIGHT_CLICK_BLOCK ? 2 : -1;

					if (pos == -1) {
						return;
					}

					this.manager.selectPosition(e.getPlayer(), pos, Position.of(e.getClickedBlock()));
				}).bindWith(this.core);
	}
}
