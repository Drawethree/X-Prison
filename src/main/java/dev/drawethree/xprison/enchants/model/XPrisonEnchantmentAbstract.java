package dev.drawethree.xprison.enchants.model;

import dev.drawethree.xprison.api.enchants.model.RefundableEnchant;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantmentGuiProperties;
import lombok.Getter;

@Getter
public abstract class XPrisonEnchantmentAbstract implements XPrisonEnchantment, RefundableEnchant {

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

	public XPrisonEnchantmentAbstract() {

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
