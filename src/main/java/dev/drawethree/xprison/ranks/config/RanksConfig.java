package dev.drawethree.xprison.ranks.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.ranks.model.RankImpl;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class RanksConfig {

	private final XPrisonRanks plugin;
	private final FileManager.Config config;
	private final Map<Integer, RankImpl> ranksById;
	private Map<String, String> messages;
	private RankImpl defaultRankImpl;
	private RankImpl maxRankImpl;
	private boolean useTokensCurrency;
	private String progressBarDelimiter;
	private int progressBarLength;

	public RanksConfig(XPrisonRanks plugin) {
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

		boolean defaultSet = false;
		if (section != null) {
			for (String key : section.getKeys(false)) {
				String rootPath = "Ranks." + key + ".";
				int id = Integer.parseInt(key);
				String prefix = TextUtils.applyColor(configuration.getString(rootPath + "Prefix"));
				long cost = configuration.getLong(rootPath + "Cost");
				List<String> commands = configuration.getStringList(rootPath + "CMD");
				RankImpl rankImpl = new RankImpl(id, cost, prefix, commands);
				this.ranksById.put(id, rankImpl);

				if (!defaultSet) {
					this.defaultRankImpl = rankImpl;
					defaultSet = true;
				}

				this.maxRankImpl = rankImpl;
			}
		}
		info(String.format("&aLoaded &e%d Ranks.", ranksById.keySet().size()));
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
		info("&fUsing &e" + (useTokensCurrency ? "Tokens" : "Money") + " &fcurrency for Ranks.");
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public RankImpl getMaxRank() {
		return maxRankImpl;
	}

	public RankImpl getDefaultRank() {
		return defaultRankImpl;
	}

	public String getProgressBarDelimiter() {
		return progressBarDelimiter;
	}

	public int getProgressBarLength() {
		return progressBarLength;
	}

	public RankImpl getRankById(int id) {
		return this.ranksById.get(id);
	}

	public boolean isUseTokensCurrency() {
		return this.useTokensCurrency;
	}
}
