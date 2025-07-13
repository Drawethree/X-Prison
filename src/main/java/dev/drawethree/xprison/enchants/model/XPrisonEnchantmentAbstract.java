package dev.drawethree.xprison.enchants.model;


import lombok.Getter;

@Getter
public abstract class XPrisonEnchantmentAbstract implements XPrisonEnchantment, RefundableEnchant, RequiresPickaxeLevel {

	protected int id;
	protected String rawName;
	protected String name;
	protected String nameWithoutColor;
	protected XPrisonEnchantmentGuiProperties guiProperties;
	protected boolean enabled;
	protected int maxLevel;
	protected long baseCost;
	protected long increaseCost;
	protected boolean refundEnabled;
	protected int refundGuiSlot;
	protected double refundPercentage;
	protected int requiredPickaxeLevel;
	protected CurrencyType currencyType;

	public XPrisonEnchantmentAbstract() {

	}

	public int getMaxLevel() {
		return this.maxLevel == -1 ? Integer.MAX_VALUE : this.maxLevel;
	}

}
