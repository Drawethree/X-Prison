package dev.drawethree.xprison.tokens.config;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokensConfig {

	private final FileManager.Config config;
	private final XPrisonTokens plugin;

	@Getter
	private double chance;
	@Getter
	private long minAmount;
	@Getter
	private long maxAmount;
	@Getter
	private long commandCooldown;
	@Getter
	private long nextResetWeekly;
	@Getter
	private long startingTokens;
	@Getter
	private int savePlayerDataInterval;
	@Getter
	private boolean displayTokenMessages;
	private Map<String, String> messages;
	@Getter
	private List<String> worldWhitelist;
	@Getter
	private List<String> tokensTopFormat;
	@Getter
	private List<String> tokenItemLore;
	@Getter
	private String tokenItemDisplayName;
	@Getter
	private ItemStack tokenItem;
	@Getter
	private int topPlayersAmount;
	@Getter
	private int tokenItemCustomModelData;

	@Getter
	private String[] tokensCommandAliases;
	@Getter
	private String[] tokensTopCommandAliases;


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
		this.topPlayersAmount = configuration.getInt("top_players_amount");
		this.worldWhitelist = configuration.getStringList("world-whitelist");
		this.tokensTopFormat = configuration.getStringList("tokens-top-format");
		this.nextResetWeekly = configuration.getLong("next-reset-weekly");
		this.displayTokenMessages = configuration.getBoolean("display-token-messages");
		this.savePlayerDataInterval = configuration.getInt("player_data_save_interval");
		this.tokenItemDisplayName = configuration.getString("tokens.item.name");
		this.tokenItemLore = configuration.getStringList("tokens.item.lore");
		this.tokenItem = XMaterial.valueOf(configuration.getString("tokens.item.material")).parseItem();
		this.startingTokens = configuration.getLong("starting-tokens");
		this.tokensCommandAliases = configuration.getStringList("tokens-command-aliases").toArray(new String[0]);
		this.tokensTopCommandAliases = configuration.getStringList("tokens-top-command-aliases").toArray(new String[0]);
		this.tokenItemCustomModelData = configuration.getInt("tokens.item.custom_model_data");
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

	public Material getTokenItemMaterial() {
		return this.tokenItem.getType();
	}

	public void save() {
		this.config.save();
	}

}
