package dev.drawethree.xprison.gangs.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GangsConfig {

	private final XPrisonGangs plugin;
	private final FileManager.Config config;

	private Map<String, String> messages;
	private Map<String, String> placeholders;
	@Getter
	private String gangDisbandGUITitle;
	@Getter
	private List<String> gangInfoFormat;
	@Getter
	private List<String> gangTopFormat;
	@Getter
	private List<String> gangAdminHelpMenu;
	@Getter
	private List<String> gangHelpMenu;
	@Getter
	private List<String> restrictedNames;
	@Getter
	private EventPriority gangChatPriority;
	@Getter
	private int maxGangMembers;
	@Getter
	private int gangUpdateDelay;
	@Getter
	private int maxGangNameLength;
	@Getter
	private boolean enableColorCodes;
	@Getter
	private boolean gangFriendlyFire;
	@Getter
	private String[] gangsCommandAliases;


	public GangsConfig(XPrisonGangs plugin) {
		this.plugin = plugin;
		this.config = this.plugin.getCore().getFileManager().getConfig("gangs.yml").copyDefaults(true).save();
	}

	private void loadMessages() {
		messages = new HashMap<>();

		YamlConfiguration configuration = getYamlConfig();

		for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(configuration.getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), "No message with key '" + key + "' found");
	}

	private void loadVariables() {

		this.loadMessages();
		this.loadPlaceholders();

		this.gangInfoFormat = this.getYamlConfig().getStringList("gang-info-format");
		this.gangHelpMenu = this.getYamlConfig().getStringList("gang-help-menu");
		this.gangDisbandGUITitle = this.getYamlConfig().getString("gang-disband-gui-title");
		this.gangAdminHelpMenu = this.getYamlConfig().getStringList("gang-admin-help-menu");
		this.gangTopFormat = this.getYamlConfig().getStringList("gang-top-format");
		this.gangUpdateDelay = this.getYamlConfig().getInt("gang-top-update", 1);
		this.maxGangMembers = this.getYamlConfig().getInt("max-gang-members", 10);
		this.maxGangNameLength = this.getYamlConfig().getInt("max-gang-name-length", 10);
		this.enableColorCodes = this.getYamlConfig().getBoolean("color-codes-in-gang-name");
		this.gangFriendlyFire = this.getYamlConfig().getBoolean("gang-friendly-fire");
		this.gangChatPriority = EventPriority.valueOf(this.getYamlConfig().getString("gang-chat-priority"));
		this.gangsCommandAliases = this.getYamlConfig().getStringList("gangs-command-aliases").toArray(new String[0]);
		this.restrictedNames = this.getYamlConfig().getStringList("restricted-names");
	}

	private FileManager.Config getConfig() {
		return this.config;
	}

	public YamlConfiguration getYamlConfig() {
		return this.config.get();
	}

	public void load() {
		this.getConfig().reload();
		this.loadVariables();
	}

	private void loadPlaceholders() {
		this.placeholders = new HashMap<>();
		for (String key : this.config.get().getConfigurationSection("placeholders").getKeys(false)) {
			this.placeholders.put(key.toLowerCase(), TextUtils.applyColor(this.config.get().getString("placeholders." + key)));
		}
	}

	public String getPlaceholder(String name) {
		return this.placeholders.get(name.toLowerCase());
	}

	public void reload() {
		this.load();
	}
}
