package dev.drawethree.ultraprisoncore.enchants.model;

public interface Refundable {

	boolean isRefundEnabled();

	int getRefundGuiSlot();

	double getRefundPercentage();
}
