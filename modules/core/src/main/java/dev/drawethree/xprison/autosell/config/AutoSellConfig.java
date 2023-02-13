package dev.drawethree.xprison.autosell.config;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoSellConfig {

    private final XPrisonAutoSell plugin;
    private final FileManager.Config config;

    private Map<String, String> messages;
    private boolean enableAutosellAutomatically;
    private boolean autoSmelt;
    private int autoSellBroadcastTime;
    private List<String> autoSellBroadcastMessage;
    private boolean inventoryFullNotificationEnabled;
    private List<String> inventoryFullNotificationTitle;
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

    private FileManager.Config getConfig() {
        return this.config;
    }

    public YamlConfiguration getYamlConfig() {
        return this.config.get();
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key.toLowerCase(), "Message not found with key: " + key);
    }

    public boolean isEnableAutosellAutomatically() {
        return enableAutosellAutomatically;
    }

    public boolean isAutoSmelt() {
        return autoSmelt;
    }

    public int getAutoSellBroadcastTime() {
        return autoSellBroadcastTime;
    }

    public List<String> getAutoSellBroadcastMessage() {
        return autoSellBroadcastMessage;
    }

    public boolean isInventoryFullNotificationEnabled() {
        return inventoryFullNotificationEnabled;
    }

    public List<String> getInventoryFullNotificationTitle() {
        return inventoryFullNotificationTitle;
    }

    public String getInventoryFullNotificationMessage() {
        return inventoryFullNotificationMessage;
    }

    public void saveSellRegion(SellRegion region) {
        this.getConfig().set("regions." + region.getRegion().getId() + ".world", region.getWorld().getName());
        for (CompMaterial material : region.getSellingMaterials()) {
            double sellPrice = region.getSellPriceForMaterial(material);
            if (sellPrice <= 0.0) {
                continue;
            }
            this.getConfig().set("regions." + region.getRegion().getId() + ".items." + material.name(), sellPrice);
        }
        this.getConfig().save();
    }
}
