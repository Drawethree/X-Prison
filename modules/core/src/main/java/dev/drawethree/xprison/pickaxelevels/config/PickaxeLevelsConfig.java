package dev.drawethree.xprison.pickaxelevels.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class PickaxeLevelsConfig {

	private final XPrisonPickaxeLevels plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;
	private Map<Integer, PickaxeLevel> pickaxeLevels;
	private int progressBarLength;
	private PickaxeLevel defaultLevel;
	private PickaxeLevel maxLevel;
	private String progressBarDelimiter;


	public PickaxeLevelsConfig(XPrisonPickaxeLevels plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("pickaxe-levels.yml").copyDefaults(true).save();
	}

	public void reload() {
		this.getConfig().reload();
		this.load();
	}

	public void load() {
		this.loadVariables();
	}

	private void loadVariables() {
		this.loadMessages();
		this.loadPickaxeLevels();
		this.progressBarDelimiter = this.getConfig().get().getString("progress-bar-delimiter");
		this.progressBarLength = this.getConfig().get().getInt("progress-bar-length");
	}


	private void loadMessages() {
		this.messages = new HashMap<>();
		for (String key : this.getYamlConfig().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(this.getYamlConfig().getString("messages." + key)));
		}
	}


	private void loadPickaxeLevels() {
		this.pickaxeLevels = new LinkedHashMap<>();

		ConfigurationSection section = this.getConfig().get().getConfigurationSection("levels");
		if (section == null) {
			return;
		}

		for (String level : section.getKeys(false)) {

			int levelId = Integer.parseInt(level);

			String displayName = TextUtils.applyColor(this.getConfig().get().getString("levels." + level + ".display_name"));
			long blocksRequire = this.getConfig().get().getLong("levels." + level + ".blocks_required");
			List<String> rewards = this.getConfig().get().getStringList("levels." + level + ".rewards");


			PickaxeLevel pickaxeLevel = new PickaxeLevel(levelId, blocksRequire, displayName, rewards);

			if (levelId == 1) {
				this.defaultLevel = pickaxeLevel;
			}

			this.pickaxeLevels.put(levelId, pickaxeLevel);
			this.maxLevel = pickaxeLevel;
		}

		this.plugin.getCore().getLogger().info("Loaded " + pickaxeLevels.size() + " Pickaxe Levels.");

	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), "Message not found with key: " + key);
	}

	public int getProgressBarLength() {
		return progressBarLength;
	}

	public PickaxeLevel getDefaultLevel() {
		return defaultLevel;
	}

	public PickaxeLevel getMaxLevel() {
		return maxLevel;
	}

	public Optional<PickaxeLevel> getPickaxeLevel(int level) {
		return Optional.ofNullable(this.pickaxeLevels.get(level));
	}

	public String getProgressBarDelimiter() {
		return progressBarDelimiter;
	}
}
