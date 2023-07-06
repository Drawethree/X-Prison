package dev.drawethree.xprison.enchants.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantsConfig {

	private final XPrisonEnchants plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;

	private String excludedFormat;
	private List<String> pickaxeLore;
	private boolean allowEnchantsOutside;
	private boolean firstJoinPickaxeEnabled;
	private CompMaterial firstJoinPickaxeMaterial;
	private List<String> firstJoinPickaxeEnchants;
	private String firstJoinPickaxeName;
	private boolean keepPickaxesOnDeath;
	private List<Action> openEnchantMenuActions;

	public EnchantsConfig(XPrisonEnchants plugin) {
		this.plugin = plugin;
		this.config = plugin.getCore().getFileManager().getConfig("enchants.yml").copyDefaults(true).save();
	}

	public void reload() {
		this.getConfig().reload();
		this.load();
	}

	public void load() {
		this.loadVariables();
		this.loadMessages();
	}

	private void loadVariables() {
		this.excludedFormat = getYamlConfig().getString("Pickaxe.excluded-format", "&7[&c-&7] &8%Enchant% %Level%");
		this.pickaxeLore = getYamlConfig().getStringList("Pickaxe.lore");
		this.openEnchantMenuActions = Arrays.stream(getYamlConfig().getString("open-enchant-menu-action", "RIGHT_CLICK_AIR,RIGHT_CLICK_BLOCK").split(",")).map(s-> Action.valueOf(s.toUpperCase())).collect(Collectors.toList());
		this.allowEnchantsOutside = getYamlConfig().getBoolean("allow-enchants-outside-mine-regions");
		this.firstJoinPickaxeEnabled = getYamlConfig().getBoolean("first-join-pickaxe.enabled");
		this.firstJoinPickaxeMaterial = CompMaterial.fromString(getYamlConfig().getString("first-join-pickaxe.material"));
		this.firstJoinPickaxeEnchants = getYamlConfig().getStringList("first-join-pickaxe.enchants");
		this.firstJoinPickaxeName = getYamlConfig().getString("first-join-pickaxe.name");
		this.keepPickaxesOnDeath = getYamlConfig().getBoolean("keep-pickaxes-on-death");
	}

	private void loadMessages() {
		this.messages = new HashMap<>();
		for (String key : getYamlConfig().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key, TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
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

	public String getExcludedFormat() {
		return excludedFormat;
	}

	public List<String> getPickaxeLore() {
		return pickaxeLore;
	}

	public boolean isFirstJoinPickaxeEnabled() {
		return firstJoinPickaxeEnabled;
	}

	public CompMaterial getFirstJoinPickaxeMaterial() {
		return firstJoinPickaxeMaterial;
	}

	public List<String> getFirstJoinPickaxeEnchants() {
		return firstJoinPickaxeEnchants;
	}

	public String getFirstJoinPickaxeName() {
		return firstJoinPickaxeName;
	}

	public boolean isKeepPickaxesOnDeath() {
		return keepPickaxesOnDeath;
	}

	public List<Action> getOpenEnchantMenuActions() {
		return openEnchantMenuActions;
	}
}
