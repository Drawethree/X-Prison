package dev.drawethree.ultraprisoncore.ranks.config;

import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanksConfig {

	private final UltraPrisonRanks plugin;
	private final FileManager.Config config;
	private final Map<Integer, Rank> ranksById;
	private Map<String, String> messages;
	private Rank maxRank;
	private boolean useTokensCurrency;
	private String progressBarDelimiter;
	private int progressBarLength;

	public RanksConfig(UltraPrisonRanks plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("ranks.yml").copyDefaults(true).save();
		this.ranksById = new HashMap<>();
	}


	private void loadMessages(YamlConfiguration configuration) {
		this.messages = new HashMap<>();

		for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
	}

	public void reload() {
		YamlConfiguration configuration = getYamlConfig();
		this.loadVariables(configuration);
		this.loadRanks(configuration);
		this.loadMessages(configuration);
	}

	private void loadRanks(YamlConfiguration configuration) {
		this.ranksById.clear();
		ConfigurationSection section = configuration.getConfigurationSection("Ranks");

		if (section != null) {
			for (String key : section.getKeys(false)) {
				String rootPath = "Ranks." + key + ".";
				int id = Integer.parseInt(key);
				String prefix = TextUtils.applyColor(configuration.getString(rootPath + "Prefix"));
				long cost = configuration.getLong(rootPath + "Cost");
				List<String> commands = configuration.getStringList(rootPath + "CMD");
				Rank rank = new Rank(id, cost, prefix, commands);
				this.ranksById.put(id, rank);
				this.maxRank = rank;
			}
		}
		this.plugin.getCore().getLogger().info(String.format("Loaded %d ranks!", ranksById.keySet().size()));
	}

	public void load() {
		this.reload();
	}


	public String getMessage(String messageKey) {
		return this.messages.getOrDefault(messageKey.toLowerCase(), "Missing message with key: " + messageKey);
	}

	private void loadVariables(YamlConfiguration configuration) {
		this.useTokensCurrency = configuration.getBoolean("use_tokens_currency");
		this.progressBarDelimiter = configuration.getString("progress-bar-delimiter");
		this.progressBarLength = configuration.getInt("progress-bar-length");
		this.plugin.getCore().getLogger().info("Using " + (useTokensCurrency ? "Tokens" : "Money") + " currency for Ranks.");
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public Rank getMaxRank() {
		return maxRank;
	}

	public String getProgressBarDelimiter() {
		return progressBarDelimiter;
	}

	public int getProgressBarLength() {
		return progressBarLength;
	}

	public Rank getRankById(int id) {
		return this.ranksById.get(id);
	}

	public boolean isUseTokensCurrency() {
		return this.useTokensCurrency;
	}
}
