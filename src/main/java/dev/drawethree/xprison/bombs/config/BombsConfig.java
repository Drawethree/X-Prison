package dev.drawethree.xprison.bombs.config;

import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class BombsConfig {

	private final XPrisonBombs plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;
	@Getter
	private int bombThrowCooldown;
	@Getter
	private String[] bombsCommandAliases;

	public BombsConfig(XPrisonBombs plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("bombs.yml").copyDefaults(true).save();
	}

	public void load() {
		loadVariables();
		loadMessages();
	}

	private void loadVariables() {
		this.bombThrowCooldown = this.getYamlConfig().getInt("bomb-throw-cooldown", 0);
		this.bombsCommandAliases = this.getYamlConfig().getStringList("bombs-command-aliases").toArray(new String[0]);
	}

	private void loadMessages() {
		this.messages = new HashMap<>();
		YamlConfiguration configuration = getYamlConfig();

		for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
			this.messages.put(key.toLowerCase(), TextUtils.applyColor(configuration.getString("messages." + key)));
		}
	}

	public void reload() {
		load();
	}

	public String getMessage(String messageKey) {
		return this.messages.getOrDefault(messageKey.toLowerCase(), TextUtils.applyColor(String.format("&cMessage with key '%s' not found!", messageKey)));
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

}
