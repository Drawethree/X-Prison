package dev.drawethree.ultraprisoncore.prestiges;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPI;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPIImpl;
import dev.drawethree.ultraprisoncore.prestiges.manager.PrestigeManager;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public final class UltraPrisonPrestiges implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_Prestiges";
	public static final String MODULE_NAME = "Prestiges";

	@Getter
	private FileManager.Config config;

	private PrestigeManager prestigeManager;

	@Getter
	private UltraPrisonPrestigesAPI api;

	private HashMap<String, String> messages;

	@Getter
	private UltraPrisonCore core;
	private boolean enabled;

	public UltraPrisonPrestiges(UltraPrisonCore UltraPrisonCore) {
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
		this.prestigeManager.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.config = this.core.getFileManager().getConfig("prestiges.yml").copyDefaults(true).save();

		this.loadMessages();
		this.prestigeManager = new PrestigeManager(this);
		api = new UltraPrisonPrestigesAPIImpl(this);
		this.registerCommands();
		this.prestigeManager.loadAllData();
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(this.getConfig().get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), TextUtils.applyColor("&cMessage " + key + " not found."));
	}


	@Override
	public void disable() {
		this.prestigeManager.stopUpdating();
		this.prestigeManager.saveAllDataSync();
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
			case MYSQL:
			case SQLITE: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_prestige bigint, primary key (UUID))"
				};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerCommands() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.prestigeManager.buyNextPrestige(c.sender());
					}
				}).registerAndBind(core, "prestige");
		Commands.create()
				.assertPermission("ultraprison.prestiges.maxprestige", this.getMessage("no_permission"))
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {

						if (this.prestigeManager.isPrestiging(c.sender())) {
							return;
						}

						this.prestigeManager.buyMaxPrestige(c.sender());
					}
				}).registerAndBind(core, "maxprestige", "maxp", "mp");
		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.prestigeManager.sendPrestigeTop(c.sender());
					}
				}).registerAndBind(core, "prestigetop");
		Commands.create()
				.assertPermission("ultraprison.prestiges.admin")
				.handler(c -> {
					if (c.args().size() == 3) {

						Player target = c.arg(1).parseOrFail(Player.class);
						int amount = c.arg(2).parseOrFail(Integer.class);

						switch (c.rawArg(0).toLowerCase()) {
							case "set":
								this.prestigeManager.setPlayerPrestige(c.sender(), target, amount);
								break;
							case "add":
								this.prestigeManager.addPlayerPrestige(c.sender(), target, amount);
								break;
							case "remove":
								this.prestigeManager.removePlayerPrestige(c.sender(), target, amount);
								break;
							default:
								PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
								PlayerUtils.sendMessage(c.sender(), "&e&lPRESTIGE ADMIN HELP MENU ");
								PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
								PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin add [player] [amount]");
								PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin remove [player] [amount]");
								PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin set [player] [amount]");
								PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
								break;
						}
					}
				}).registerAndBind(core, "prestigeadmin", "prestigea");
	}
}
