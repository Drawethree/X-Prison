package dev.drawethree.xprison.pickaxelevels.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevelImpl;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class PickaxeLevelsConfig {

	private final XPrisonPickaxeLevels plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;
	private Map<Integer, PickaxeLevelImpl> pickaxeLevels;
	private int progressBarLength;
	private PickaxeLevelImpl defaultLevel;
	private PickaxeLevelImpl maxLevel;
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


			PickaxeLevelImpl pickaxeLevelImpl = new PickaxeLevelImpl(levelId, blocksRequire, displayName, rewards);

			if (levelId == 1) {
				this.defaultLevel = pickaxeLevelImpl;
			}

			this.pickaxeLevels.put(levelId, pickaxeLevelImpl);
			this.maxLevel = pickaxeLevelImpl;
		}

		info("&aLoaded &e" + pickaxeLevels.size() + " Pickaxe Levels.");

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

	public PickaxeLevelImpl getDefaultLevel() {
		return defaultLevel;
	}

	public PickaxeLevelImpl getMaxLevel() {
		return maxLevel;
	}

	public Optional<PickaxeLevelImpl> getPickaxeLevel(int level) {
		return Optional.ofNullable(this.pickaxeLevels.get(level));
	}

	public String getProgressBarDelimiter() {
		return progressBarDelimiter;
	}
}
