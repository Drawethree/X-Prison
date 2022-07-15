package dev.drawethree.ultraprisoncore.gems;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPI;
import dev.drawethree.ultraprisoncore.gems.api.UltraPrisonGemsAPIImpl;
import dev.drawethree.ultraprisoncore.gems.managers.CommandManager;
import dev.drawethree.ultraprisoncore.gems.managers.GemsManager;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
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
	private final UltraPrisonCore core;

	private HashMap<String, String> messages;

	private boolean enabled;
	private CommandManager commandManager;

	@Getter
	private long commandCooldown;

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
		this.loadVariables();

		this.gemsManager.reload();
		this.commandManager.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("gems.yml").copyDefaults(true).save();

		this.loadVariables();
		this.loadMessages();

		this.gemsManager = new GemsManager(this);
		this.commandManager = new CommandManager(this);
		this.commandManager.enable();
		this.api = new UltraPrisonGemsAPIImpl(this.gemsManager);

		this.registerEvents();
	}

	private void loadVariables() {
		this.commandCooldown = getConfig().get().getLong("gems-command-cooldown");
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


	private void loadMessages() {
		this.messages = new HashMap<>();
		for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			this.messages.put(key, TextUtils.applyColor(this.getConfig().get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return this.messages.get(key);
	}
}
