package dev.drawethree.xprison.tokens.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.tokens.model.BlockReward;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockRewardsConfig {

	private final XPrisonTokens plugin;
	private final FileManager.Config config;
	private final Map<Long, BlockReward> blockRewards;

	public BlockRewardsConfig(XPrisonTokens plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("block-rewards.yml").copyDefaults(true).save();
		this.blockRewards = new LinkedHashMap<>();
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public void load() {
		YamlConfiguration configuration = getYamlConfig();
		this.loadVariables(configuration);
	}

	public void reload() {
		this.getConfig().reload();
		this.load();
	}

	private void loadVariables(YamlConfiguration configuration) {
		this.blockRewards.clear();
		ConfigurationSection section = configuration.getConfigurationSection("block-rewards");

		if (section != null) {
			for (String key : section.getKeys(false)) {
				long blocksNeeded = Long.parseLong(key);
				String message = TextUtils.applyColor(configuration.getString("block-rewards." + key + ".message"));
				List<String> commands = configuration.getStringList("block-rewards." + key + ".commands");
				BlockReward reward = new BlockReward(blocksNeeded, commands, message);
				this.blockRewards.put(blocksNeeded, reward);
			}
		}
		this.plugin.getCore().getLogger().info("Loaded " + this.blockRewards.keySet().size() + " Block Rewards!");
	}

	public Map<Long, BlockReward> getBlockRewards() {
		return blockRewards;
	}
}
