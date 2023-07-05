package dev.drawethree.xprison.enchants.model;

public interface Refundable {

	boolean isRefundEnabled();

	int getRefundGuiSlot();

	double getRefundPercentage();
}
