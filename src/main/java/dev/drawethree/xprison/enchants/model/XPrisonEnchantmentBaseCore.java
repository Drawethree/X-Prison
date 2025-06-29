package dev.drawethree.xprison.enchants.model;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.enchants.XPrisonEnchants;

import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Setter;
import org.bukkit.Material;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        this.id = config.get("id").getAsInt();
        this.rawName = config.get("rawName").getAsString();
        this.name = TextUtils.applyColor(config.get("name").getAsString());
        this.nameWithoutColor = this.name.replaceAll("§.", "");
        this.enabled = config.get("enabled").getAsBoolean();
        this.maxLevel = config.get("maxLevel").getAsInt();
        this.baseCost = config.get("initialCost").getAsLong();
        this.increaseCost = config.get("increaseCostBy").getAsLong();
        this.refundEnabled = config.get("refund").getAsJsonObject().get("enabled").getAsBoolean();
        this.refundGuiSlot = config.get("refund").getAsJsonObject().get("guiSlot").getAsInt();
        this.refundPercentage = config.get("refund").getAsJsonObject().get("percentage").getAsDouble();
    }

    private void loadGuiProperties(JsonObject config) {
        JsonObject guiObject = config.get("gui").getAsJsonObject();
        int guiSlot = guiObject.get("slot").getAsInt();
        Material guiMaterial = XMaterial.valueOf(guiObject.get("material").getAsString()).get();
        String guiName = TextUtils.applyColor(guiObject.get("name").getAsString());
        String guiBase64 = guiObject.get("base64") != null ? guiObject.get("base64").getAsString() : null;
        List<String> description = new Gson().fromJson(
                guiObject.get("description"),
                new TypeToken<List<String>>(){}.getType()
        );
        List<String> guiDescription = TextUtils.applyColor(description);
        this.guiProperties = new XPrisonEnchantmentGuiPropertiesImpl(guiSlot,guiName,guiBase64,guiMaterial,guiDescription);
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