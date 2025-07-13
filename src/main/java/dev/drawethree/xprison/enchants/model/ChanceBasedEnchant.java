package dev.drawethree.xprison.enchants.model;

/**
 * Interface for enchantments that trigger based on a chance percentage.
 */
public interface ChanceBasedEnchant {

    /**
     * Returns the chance (0.00 - 100.00) for the enchantment to trigger based on its level.
     *
     * @param enchantLevel the level of the enchantment
     * @return chance to trigger as a percentage (0.00 to 100.00)
     */
    double getChanceToTrigger(int enchantLevel);
}
