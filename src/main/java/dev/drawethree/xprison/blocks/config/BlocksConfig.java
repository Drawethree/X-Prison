package dev.drawethree.xprison.blocks.config;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.blocks.XPrisonBlocks;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlocksConfig {

	private final FileManager.Config config;
	private final XPrisonBlocks plugin;

	private long commandCooldown;
	private long nextResetWeekly;
	private int savePlayerDataInterval;
	private Map<String, String> messages;
	private Map<Material, List<String>> luckyBlockRewards;
	private List<String> blocksTopFormat;
	private List<String> blocksTopFormatWeekly;
	private int topPlayersAmount;
	private String[] blocksTopCommandAliases;


	public BlocksConfig(XPrisonBlocks plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("blocks.yml").copyDefaults(true).save();
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}


	private void loadVariables(YamlConfiguration configuration) {
		this.commandCooldown = configuration.getLong("blocks-command-cooldown");
		this.luckyBlockRewards = new HashMap<>();

		for (String key : configuration.getConfigurationSection("lucky-blocks").getKeys(false)) {
			XMaterial material = XMaterial.valueOf(key);
			List<String> rewards = configuration.getStringList("lucky-blocks." + key);
			if (rewards.isEmpty()) {
				continue;
			}
			this.luckyBlockRewards.put(material.get(), rewards);
		}

		this.topPlayersAmount = configuration.getInt("top_players_amount");
		this.blocksTopFormat = configuration.getStringList("blocks-top-format");
		this.blocksTopFormatWeekly = configuration.getStringList("blocks-top-weekly-format");
		this.nextResetWeekly = configuration.getLong("next-reset-weekly");
		this.savePlayerDataInterval = configuration.getInt("player_data_save_interval");
		this.blocksTopCommandAliases = configuration.getStringList("blocks-top-command-aliases").toArray(new String[0]);

	}

	private void loadMessages(YamlConfiguration configuration) {
		this.messages = new HashMap<>();

		for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
	}

	public void reload() {
		this.config.reload();
		YamlConfiguration configuration = getYamlConfig();
		this.loadVariables(configuration);
		this.loadMessages(configuration);
	}

	public void load() {
		this.reload();
	}

	public String getMessage(String messageKey) {
		return this.messages.getOrDefault(messageKey.toLowerCase(), "Missing message with key: " + messageKey);
	}

	public List<String> getBlocksTopFormat() {
		return blocksTopFormat;
	}

	public List<String> getBlocksTopFormatWeekly() {
		return blocksTopFormatWeekly;
	}

	public List<String> getLuckyBlockReward(Material m) {
		return this.luckyBlockRewards.getOrDefault(m, new ArrayList<>());
	}

	public long getCommandCooldown() {
		return commandCooldown;
	}

	public long getNextResetWeekly() {
		return nextResetWeekly;
	}

	public int getTopPlayersAmount() {
		return topPlayersAmount;
	}

	public String[] getBlocksTopCommandAliases() {
		return blocksTopCommandAliases;
	}

	public void save() {
		this.config.save();
	}

	public void setNextResetWeekly(long time) {
		this.nextResetWeekly = time;

	}

	public int getSavePlayerDataInterval() {
		return savePlayerDataInterval;
	}
}
