package dev.drawethree.xprison.tokens.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokensConfig {

	private final FileManager.Config config;
	private final XPrisonTokens plugin;

	private double chance;
	private long minAmount;
	private long maxAmount;
	private long commandCooldown;
	private long nextResetWeekly;
	private long startingTokens;
	private int savePlayerDataInterval;
	private boolean displayTokenMessages;
	private Map<String, String> messages;
	private Map<Material, List<String>> luckyBlockRewards;
	private List<String> worldWhitelist;
	private List<String> tokensTopFormat;
	private List<String> blocksTopFormat;
	private List<String> blocksTopFormatWeekly;
	private List<String> tokenItemLore;
	private String tokenItemDisplayName;
	private ItemStack tokenItem;
	private int topPlayersAmount;

	private String[] tokensCommandAliases;
	private String[] tokensTopCommandAliases;
	private String[] blocksTopCommandAliases;


	public TokensConfig(XPrisonTokens plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("tokens.yml").copyDefaults(true).save();
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}


	private void loadVariables(YamlConfiguration configuration) {
		this.chance = configuration.getDouble("tokens.breaking.chance");
		this.minAmount = configuration.getLong("tokens.breaking.min");
		this.maxAmount = configuration.getLong("tokens.breaking.max");

		this.commandCooldown = configuration.getLong("tokens-command-cooldown");

		this.luckyBlockRewards = new HashMap<>();

		for (String key : configuration.getConfigurationSection("lucky-blocks").getKeys(false)) {
			CompMaterial material = CompMaterial.fromString(key);
			List<String> rewards = configuration.getStringList("lucky-blocks." + key);
			if (rewards.isEmpty()) {
				continue;
			}
			this.luckyBlockRewards.put(material.toMaterial(), rewards);
		}

		this.topPlayersAmount = configuration.getInt("top_players_amount");
		this.worldWhitelist = configuration.getStringList("world-whitelist");
		this.tokensTopFormat = configuration.getStringList("tokens-top-format");
		this.blocksTopFormat = configuration.getStringList("blocks-top-format");
		this.blocksTopFormatWeekly = configuration.getStringList("blocks-top-weekly-format");
		this.nextResetWeekly = configuration.getLong("next-reset-weekly");
		this.displayTokenMessages = configuration.getBoolean("display-token-messages");
		this.savePlayerDataInterval = configuration.getInt("player_data_save_interval");
		this.tokenItemDisplayName = configuration.getString("tokens.item.name");
		this.tokenItemLore = configuration.getStringList("tokens.item.lore");
		this.tokenItem = CompMaterial.fromString(configuration.getString("tokens.item.material")).toItem();
		this.startingTokens = configuration.getLong("starting-tokens");
		this.tokensCommandAliases = configuration.getStringList("tokens-command-aliases").toArray(new String[0]);
		this.tokensTopCommandAliases = configuration.getStringList("tokens-top-command-aliases").toArray(new String[0]);
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

	public boolean isDisplayTokenMessages() {
		return this.displayTokenMessages;
	}

	public Material getTokenItemMaterial() {
		return this.tokenItem.getType();
	}

	public long getStartingTokens() {
		return this.startingTokens;
	}

	public List<String> getWorldWhitelist() {
		return worldWhitelist;
	}

	public List<String> getTokensTopFormat() {
		return tokensTopFormat;
	}

	public List<String> getBlocksTopFormat() {
		return blocksTopFormat;
	}

	public List<String> getBlocksTopFormatWeekly() {
		return blocksTopFormatWeekly;
	}

	public List<String> getTokenItemLore() {
		return tokenItemLore;
	}

	public String getTokenItemDisplayName() {
		return tokenItemDisplayName;
	}

	public List<String> getLuckyBlockReward(Material m) {
		return this.luckyBlockRewards.getOrDefault(m, new ArrayList<>());
	}

	public double getChance() {
		return chance;
	}

	public long getMinAmount() {
		return minAmount;
	}

	public long getMaxAmount() {
		return maxAmount;
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

	public ItemStack getTokenItem() {
		return tokenItem;
	}

	public String[] getTokensCommandAliases() {
		return tokensCommandAliases;
	}

	public String[] getTokensTopCommandAliases() {
		return tokensTopCommandAliases;
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
