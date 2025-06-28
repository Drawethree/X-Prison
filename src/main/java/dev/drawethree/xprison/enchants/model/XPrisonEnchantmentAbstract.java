package dev.drawethree.xprison.enchants.model;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.api.enchants.model.RefundableEnchant;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevelImpl;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public abstract class XPrisonEnchantmentAbstract implements XPrisonEnchantment, RefundableEnchant {

	protected final XPrisonEnchants plugin;

	protected final int id;
	private String rawName;
	private String name;
	private String nameWithoutColor;
	private String guiName;
	private String guiBase64;
	private Material guiMaterial;
	private List<String> guiDescription;
	private boolean enabled;
	private int guiSlot;
	private int maxLevel;
	private long baseCost;
	private long increaseCost;
	private int requiredPickaxeLevel;
	private boolean refundEnabled;
	private int refundGuiSlot;
	private double refundPercentage;

	public XPrisonEnchantmentAbstract(XPrisonEnchants plugin, int id) {
		this.plugin = plugin;
		this.id = id;
		this.reloadDefaultAttributes();
		this.reload();
	}

	private void reloadDefaultAttributes() {
		this.rawName = this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".RawName");
		this.name = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Name"));
		this.nameWithoutColor = this.name.replaceAll("§.", "");
		this.guiName = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".GuiName"));
		this.guiMaterial = XMaterial.valueOf(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Material")).get();
		this.guiDescription = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Description"));
		this.enabled = this.plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Enabled");
		this.guiSlot = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".InGuiSlot");
		this.maxLevel = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".Max");
		this.baseCost = this.plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Cost");
		this.increaseCost = this.plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Increase-Cost-by");
		this.requiredPickaxeLevel = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".Pickaxe-Level-Required");
		this.guiBase64 = this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Base64", null);
		this.refundEnabled = this.plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + this.id + ".Refund.Enabled", true);
		this.refundGuiSlot = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + this.id + ".Refund.InGuiSlot");
		this.refundPercentage = this.plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + this.id + ".Refund.Percentage", 100.0d);
	}

	public abstract String getAuthor();

	public abstract double getChanceToTrigger(int enchantLevel);

	public void reload() {
		this.reloadDefaultAttributes();
	}

	@Override
	public boolean isRefundEnabled() {
		return refundEnabled;
	}

	@Override
	public double getRefundPercentage() {
		return refundPercentage;
	}

	@Override
	public int getRefundGuiSlot() {
		return refundGuiSlot;
	}

	public int getMaxLevel() {
		return this.maxLevel == -1 ? Integer.MAX_VALUE : this.maxLevel;
	}


}
