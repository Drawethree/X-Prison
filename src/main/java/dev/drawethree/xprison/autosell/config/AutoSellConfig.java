package dev.drawethree.xprison.autosell.config;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoSellConfig {

    private final XPrisonAutoSell plugin;
    private final FileManager.Config config;

    private Map<String, String> messages;
    @Getter
    private boolean enableAutosellAutomatically;
    @Getter
    private boolean autoSmelt;
    @Getter
    private int autoSellBroadcastTime;
    @Getter
    private List<String> autoSellBroadcastMessage;
    @Getter
    private boolean inventoryFullNotificationEnabled;
    @Getter
    private List<String> inventoryFullNotificationTitle;
    @Getter
    private String inventoryFullNotificationMessage;


    public AutoSellConfig(XPrisonAutoSell plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getCore().getFileManager().getConfig("autosell.yml").copyDefaults(true).save();
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
        this.autoSellBroadcastTime = this.getYamlConfig().getInt("auto_sell_broadcast.time");
        this.autoSellBroadcastMessage = this.getYamlConfig().getStringList("auto_sell_broadcast.message");
        this.enableAutosellAutomatically = this.getYamlConfig().getBoolean("enable-autosell-automatically");
        this.autoSmelt = this.getYamlConfig().getBoolean("auto-smelt");
        this.inventoryFullNotificationEnabled = this.getYamlConfig().getBoolean("inventory_full_notification.enabled");
        this.inventoryFullNotificationTitle = this.getYamlConfig().getStringList("inventory_full_notification.title");
        this.inventoryFullNotificationMessage = this.getYamlConfig().getString("inventory_full_notification.chat");
    }

    private void loadMessages() {
        this.messages = new HashMap<>();
        for (String key : this.getYamlConfig().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), TextUtils.applyColor(this.getYamlConfig().getString("messages." + key)));
        }
    }

    public FileManager.Config getConfig() {
        return this.config;
    }

    public YamlConfiguration getYamlConfig() {
        return this.config.get();
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key.toLowerCase(), "Message not found with key: " + key);
    }
}
