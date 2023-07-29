package dev.drawethree.xprison.gems;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.gems.api.XPrisonGemsAPI;
import dev.drawethree.xprison.gems.api.XPrisonGemsAPIImpl;
import dev.drawethree.xprison.gems.managers.CommandManager;
import dev.drawethree.xprison.gems.managers.GemsManager;
import dev.drawethree.xprison.gems.repo.GemsRepository;
import dev.drawethree.xprison.gems.repo.impl.GemsRepositoryImpl;
import dev.drawethree.xprison.gems.service.GemsService;
import dev.drawethree.xprison.gems.service.impl.GemsServiceImpl;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;

public final class XPrisonGems implements XPrisonModule {

	public static final String MODULE_NAME = "Gems";
	public static final String GEMS_ADMIN_PERM = "xprison.gems.admin";

	@Getter
	private static XPrisonGems instance;

	@Getter
	private FileManager.Config config;

	@Getter
	private XPrisonGemsAPI api;

	@Getter
	private GemsManager gemsManager;
	@Getter
	private final XPrison core;

	@Getter
	private GemsRepository gemsRepository;

	@Getter
	private GemsService gemsService;

	private HashMap<String, String> messages;

	private boolean enabled;
	private CommandManager commandManager;

	@Getter
	private long commandCooldown;

	public XPrisonGems(XPrison XPrison) {
		instance = this;
		this.core = XPrison;
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

		this.gemsRepository = new GemsRepositoryImpl(this.core.getPluginDatabase());
		this.gemsRepository.createTables();
		this.gemsService = new GemsServiceImpl(this.gemsRepository);
		this.gemsManager = new GemsManager(this);
		this.commandManager = new CommandManager(this);
		this.commandManager.enable();
		this.api = new XPrisonGemsAPIImpl(this.gemsManager);

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
	public boolean isHistoryEnabled() {
		return true;
	}

	@Override
	public void resetPlayerData() {
		this.gemsRepository.clearTableData();
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
