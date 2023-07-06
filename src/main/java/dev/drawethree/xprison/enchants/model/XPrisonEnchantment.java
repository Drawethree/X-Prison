package dev.drawethree.xprison.enchants.model;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public abstract class XPrisonEnchantment implements Refundable {

	protected final XPrisonEnchants plugin;

	protected final int id;
	private String rawName;
	private String name;
	private String nameUncolor;
	private String guiName;
	private String base64;
	private Material material;
	private List<String> description;
	private boolean enabled;
	private int guiSlot;
	private int maxLevel;
	private long cost;
	private long increaseCost;
	private int requiredPickaxeLevel;
	private boolean messagesEnabled;
	private boolean refundEnabled;
	private int refundGuiSlot;
	private double refundPercentage;

	public XPrisonEnchantment(XPrisonEnchants plugin, int id) {
		this.plugin = plugin;
		this.id = id;
		this.reloadDefaultAttributes();
		this.reload();
	}

	private void reloadDefaultAttributes() {
		this.rawName = this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".RawName");
		this.name = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Name"));
		this.nameUncolor = this.name.replaceAll("§.", "");
		this.guiName = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".GuiName"));
		this.material = CompMaterial.fromString(this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Material")).toMaterial();
		this.description = TextUtils.applyColor(this.plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Description"));
		this.enabled = this.plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Enabled");
		this.guiSlot = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".InGuiSlot");
		this.maxLevel = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".Max");
		this.cost = this.plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Cost");
		this.increaseCost = this.plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Increase-Cost-by");
		this.requiredPickaxeLevel = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + id + ".Pickaxe-Level-Required");
		this.messagesEnabled = this.plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + id + ".Messages-Enabled", true);
		this.base64 = this.plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Base64", null);
		this.refundEnabled = this.plugin.getEnchantsConfig().getYamlConfig().getBoolean("enchants." + this.id + ".Refund.Enabled", true);
		this.refundGuiSlot = this.plugin.getEnchantsConfig().getYamlConfig().getInt("enchants." + this.id + ".Refund.InGuiSlot");
		this.refundPercentage = this.plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + this.id + ".Refund.Percentage", 100.0d);
	}

	public abstract String getAuthor();

	public abstract void onEquip(Player p, ItemStack pickAxe, int level);

	public abstract void onUnequip(Player p, ItemStack pickAxe, int level);

	public abstract void onBlockBreak(BlockBreakEvent e, int enchantLevel);

	public abstract double getChanceToTrigger(int enchantLevel);

	public void reload() {
		this.reloadDefaultAttributes();
	}

	public long getCostOfLevel(int level) {
		return (this.cost + (this.increaseCost * (level - 1)));
	}

	public long getRefundForLevel(int level) {
		return (long) (this.getCostOfLevel(level) * (this.getRefundPercentage() / 100.0));
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

	public boolean canBeBought(ItemStack pickAxe) {
		if (!this.plugin.getCore().isModuleEnabled(XPrisonPickaxeLevels.MODULE_NAME)) {
			return true;
		}
		Optional<PickaxeLevel> pickaxeLevelOptional = this.plugin.getCore().getPickaxeLevels().getApi().getPickaxeLevel(pickAxe);
		return pickaxeLevelOptional.map(level -> level.getLevel() >= this.requiredPickaxeLevel).orElse(true);
	}
}
