package dev.drawethree.xprison.enchants.model;

/**
 * Interface representing enchantments that support refunding mechanics.
 */
public interface RefundableEnchant {

    /**
     * Indicates whether refunding this enchantment is enabled.
     *
     * @return true if refunding is enabled, false otherwise.
     */
    boolean isRefundEnabled();

    /**
     * Gets the GUI slot position where this enchantment's refund option appears
     * in the disenchanting GUI.
     *
     * @return the slot index in the disenchant GUI.
     */
    int getRefundGuiSlot();

    /**
     * Gets the percentage of the original enchantment price that will be refunded
     * upon disenchanting.
     * Value ranges between 0.00 (0%) and 100.00 (100%).
     *
     * @return the refund percentage.
     */
    double getRefundPercentage();
}
