package dev.drawethree.xprison.prestiges.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.prestiges.model.Prestige;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrestigeConfig {

    private final XPrisonPrestiges plugin;
    private final FileManager.Config config;
    private Prestige maxPrestige;
    private String unlimitedPrestigePrefix;
    private List<String> prestigeTopFormat;
    private List<String> unlimitedPrestigesRewardPerPrestige;
    private Map<Long, Prestige> prestigeById;
    private Map<String, String> messages;
    private Map<Long, List<String>> unlimitedPrestigesRewards;
    private int topPlayersAmount;
    private long unlimitedPrestigeMax;
    private double unlimitedPrestigeCost;
    private double increaseCostBy;
    private boolean useTokensCurrency;
    private boolean unlimitedPrestiges;
    private boolean increaseCostEnabled;
    private boolean resetRankAfterPrestige;
    private int savePlayerDataInterval;

    public PrestigeConfig(XPrisonPrestiges plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getCore().getFileManager().getConfig("prestiges.yml").copyDefaults(true).save();
        this.prestigeById = new HashMap<>();
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
        this.loadPrestiges(configuration);
        this.loadUnlimitedPrestigesRewards(configuration);
        this.loadMessages(configuration);
    }

    private void loadPrestiges(YamlConfiguration configuration) {
        this.prestigeById.clear();

        if (this.unlimitedPrestiges) {
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d prestiges.", this.unlimitedPrestigeMax));
        } else {
            for (String key : configuration.getConfigurationSection("Prestige").getKeys(false)) {
                long id = Long.parseLong(key);
                String prefix = TextUtils.applyColor(configuration.getString("Prestige." + key + ".Prefix"));
                long cost = configuration.getLong("Prestige." + key + ".Cost");
                List<String> commands = configuration.getStringList("Prestige." + key + ".CMD");
                Prestige p = new Prestige(id, cost, prefix, commands);
                this.prestigeById.put(id, p);
                this.maxPrestige = p;
            }
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d prestiges!", this.prestigeById.keySet().size()));
        }
    }

    public void load() {
        this.reload();
    }


    public String getMessage(String messageKey) {
        return this.messages.getOrDefault(messageKey.toLowerCase(), "Missing message with key: " + messageKey);
    }

    private void loadVariables(YamlConfiguration configuration) {
        this.prestigeTopFormat = configuration.getStringList("prestige-top-format");
        this.unlimitedPrestiges = configuration.getBoolean("unlimited_prestiges.enabled");
        this.unlimitedPrestigeCost = configuration.getDouble("unlimited_prestiges.prestige_cost");
        this.unlimitedPrestigePrefix = TextUtils.applyColor(configuration.getString("unlimited_prestiges.prefix"));
        this.unlimitedPrestigeMax = configuration.getLong("unlimited_prestiges.max_prestige");
        this.increaseCostEnabled = configuration.getBoolean("unlimited_prestiges.increase_cost.enabled");
        this.increaseCostBy = configuration.getDouble("unlimited_prestiges.increase_cost.increase_cost_by");
        boolean unlimitedPrestigesRewardPerPrestigeEnabled = configuration.getBoolean("unlimited_prestiges.rewards-per-prestige.enabled");
        if (unlimitedPrestigesRewardPerPrestigeEnabled) {
            this.unlimitedPrestigesRewardPerPrestige = configuration.getStringList("unlimited_prestiges.rewards-per-prestige.rewards");
        }
        this.topPlayersAmount = configuration.getInt("top_players_amount");
        this.savePlayerDataInterval = configuration.getInt("player_data_save_interval");
        this.resetRankAfterPrestige = configuration.getBoolean("reset_rank_after_prestige");
        this.useTokensCurrency = configuration.getBoolean("use_tokens_currency");
        this.plugin.getCore().getLogger().info("Using " + (useTokensCurrency ? "Tokens" : "Money") + " currency for Prestiges.");

    }

    private void loadUnlimitedPrestigesRewards(YamlConfiguration configuration) {
        this.unlimitedPrestigesRewards = new LinkedHashMap<>();

        ConfigurationSection section = configuration.getConfigurationSection("unlimited_prestiges.rewards");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                long id = Long.parseLong(key);

                List<String> rewards = section.getStringList(key);

                if (!rewards.isEmpty()) {
                    this.unlimitedPrestigesRewards.put(id, rewards);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private FileManager.Config getConfig() {
        return this.config;
    }

    public YamlConfiguration getYamlConfig() {
        return this.config.get();
    }

    public Prestige getMaxPrestige() {
        return maxPrestige;
    }

    public String getUnlimitedPrestigePrefix() {
        return unlimitedPrestigePrefix;
    }

    public List<String> getPrestigeTopFormat() {
        return prestigeTopFormat;
    }

    public List<String> getUnlimitedPrestigesRewardPerPrestige() {
        return unlimitedPrestigesRewardPerPrestige;
    }

    public Map<Long, Prestige> getPrestigeById() {
        return prestigeById;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public Map<Long, List<String>> getUnlimitedPrestigesRewards() {
        return unlimitedPrestigesRewards;
    }

    public long getUnlimitedPrestigeMax() {
        return unlimitedPrestigeMax;
    }

    public double getUnlimitedPrestigeCost() {
        return unlimitedPrestigeCost;
    }

    public double getIncreaseCostBy() {
        return increaseCostBy;
    }

    public boolean isUseTokensCurrency() {
        return useTokensCurrency;
    }

    public boolean isUnlimitedPrestiges() {
        return unlimitedPrestiges;
    }

    public boolean isIncreaseCostEnabled() {
        return increaseCostEnabled;
    }

    public boolean isResetRankAfterPrestige() {
        return resetRankAfterPrestige;
    }

    public int getTopPlayersAmount() {
        return topPlayersAmount;
    }

    public long getSavePlayerDataInterval() {
        return savePlayerDataInterval;
    }
}
