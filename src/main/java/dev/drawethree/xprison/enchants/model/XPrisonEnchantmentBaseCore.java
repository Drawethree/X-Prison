package dev.drawethree.xprison.enchants.model;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.currency.CurrencyType;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Setter;
import org.bukkit.Material;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Currency;
import java.util.List;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;


@Setter
public class XPrisonEnchantmentBaseCore extends XPrisonEnchantmentAbstract {

    private File enchantConfigFile;

    public void load(JsonObject config) {
        this.loadBaseProperties(config);
        this.loadGuiProperties(config);
        this.loadCustomProperties(config);
    }

    private void loadBaseProperties(JsonObject config) {
        this.id = JsonUtils.getInt(config,"id",-1);
        this.rawName = JsonUtils.getString(config, "rawName", "");
        this.name = TextUtils.applyColor(JsonUtils.getString(config, "name", ""));
        this.nameWithoutColor = this.name.replaceAll("§.", "");
        this.enabled = JsonUtils.getBoolean(config,"enabled",false);
        this.maxLevel = JsonUtils.getInt(config,"maxLevel",0);
        this.baseCost = JsonUtils.getLong(config,"initialCost", 0);
        this.increaseCost = JsonUtils.getLong(config,"increaseCostBy", 0);
        this.requiredPickaxeLevel = JsonUtils.getInt(config,"pickaxeLevelRequired",1);
        this.currencyType = CurrencyType.valueOf(JsonUtils.getString(config, "currency", CurrencyType.TOKENS.name()));

        JsonObject refundObject = JsonUtils.getObject(config,"refund");
        this.refundEnabled = JsonUtils.getBoolean(refundObject,"enabled", false);
        this.refundGuiSlot = JsonUtils.getInt(refundObject,"guiSlot", -1);
        this.refundPercentage = JsonUtils.getDouble(refundObject, "percentage", 0.0);
    }

    private void loadGuiProperties(JsonObject config) {
        JsonObject guiObject = config.get("gui").getAsJsonObject();
        int guiSlot = JsonUtils.getInt(guiObject, "slot", 0);
        Material guiMaterial = XMaterial.valueOf(JsonUtils.getString(guiObject, "material","BARRIER")).get();
        String guiName = TextUtils.applyColor(JsonUtils.getString(guiObject, "name", ""));
        String guiBase64 = guiObject.get("base64") != null ? guiObject.get("base64").getAsString() : null;
        List<String> description = new Gson().fromJson(
                guiObject.get("description"),
                new TypeToken<List<String>>(){}.getType()
        );
        int customModelData = JsonUtils.getInt(guiObject,"customModelData", 0);
        List<String> guiDescription = TextUtils.applyColor(description);
        this.guiProperties = new XPrisonEnchantmentGuiPropertiesImpl(guiSlot,guiName,guiBase64,guiMaterial,guiDescription,customModelData);
    }

    protected void loadCustomProperties(JsonObject config) {
        //This should be overridden in respective implementations, if any custom properties are applicable
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    @Override
    public void load() {
        try (FileReader reader = new FileReader(enchantConfigFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            load(config);
            info("Enchant configuration for enchant " + this.rawName + " loaded successfully.");
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to load enchant config: " + enchantConfigFile.getName(), e);
        }
    }

    @Override
    public void unload() {
        //Nothing to do here.
    }

    protected XPrison getCore() {
        return XPrison.getInstance();
    }

    protected XPrisonEnchants getEnchants() {
        return getCore().getEnchants();
    }

}