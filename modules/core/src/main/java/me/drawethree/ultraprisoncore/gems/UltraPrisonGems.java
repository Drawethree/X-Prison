package me.drawethree.ultraprisoncore.gems;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPI;
import me.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPIImpl;
import me.drawethree.ultraprisoncore.gems.commands.*;
import me.drawethree.ultraprisoncore.gems.managers.GemsManager;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.text.TextUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;

public final class UltraPrisonGems implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_Gems";
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

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case SQLITE:
			case MYSQL: {
				return new String[]{"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Gems bigint, primary key (UUID))"};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerEvents() {
		Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
				.filter(e -> e.getItem() != null && e.getItem().getType() == this.gemsManager.getGemsItemMaterial() && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
				.handler(e -> {
					if (e.getItem().hasItemMeta()) {
						e.setCancelled(true);
						e.setUseInteractedBlock(Event.Result.DENY);
						boolean offHandClick = false;
						if (MinecraftVersion.getRuntimeVersion().isAfter(MinecraftVersion.of(1, 8, 9))) {
							offHandClick = e.getHand() == EquipmentSlot.OFF_HAND;
						}
						this.gemsManager.redeemGems(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking(), offHandClick);
					}
				}).bindWith(core);
	}

	private void registerCommands() {

		this.commands = new HashMap<>();
		this.commands.put("give", new GemsGiveCommand(this));
		this.commands.put("add", new GemsGiveCommand(this));
		this.commands.put("remove", new GemsRemoveCommand(this));
		this.commands.put("set", new GemsSetCommand(this));
		this.commands.put("help", new GemsHelpCommand(this));
		this.commands.put("pay", new GemsPayCommand(this));
		this.commands.put("withdraw", new GemsWithdrawCommand(this));

		Commands.create()
				.handler(c -> {

					if (c.args().size() == 0) {
						if (c.sender() instanceof Player) {
							this.gemsManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
						}
						return;
					}

					GemsCommand subCommand = this.getCommand(c.rawArg(0));
					if (subCommand != null) {
						if (subCommand.canExecute(c.sender())) {
							subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
						} else {
							PlayerUtils.sendMessage(c.sender(), this.getMessage("no_permission"));
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
			messages.put(key, TextUtils.applyColor(this.getConfig().get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return messages.get(key);
	}

	private GemsCommand getCommand(String arg) {
		return commands.get(arg.toLowerCase());
	}
}
