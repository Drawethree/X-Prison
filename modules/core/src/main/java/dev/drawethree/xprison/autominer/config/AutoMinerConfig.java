package dev.drawethree.xprison.autominer.config;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class AutoMinerConfig {

	private final XPrisonAutoMiner plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;

	public AutoMinerConfig(XPrisonAutoMiner plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("autominer.yml").copyDefaults(true).save();
	}

	private void loadMessages() {
		messages = new HashMap<>();

		YamlConfiguration configuration = getYamlConfig();

		for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(configuration.getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), "No message with key '" + key + "' found");
	}

	private void loadVariables() {
		this.loadMessages();
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public void load() {
		this.getConfig().reload();
		this.loadVariables();
	}

	public void reload() {
		this.load();
	}
}
