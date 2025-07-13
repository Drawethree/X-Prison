package dev.drawethree.xprison.enchants.model;

/**
 * Represents an enchantment  that requires a minimum pickaxe level
 * to be applied or used.
 */
public interface RequiresPickaxeLevel {

    /**
     * Gets the required minimum pickaxe level needed to apply or use
     * this enchantment.
     *
     * @return the required pickaxe level as an integer
     */
    int getRequiredPickaxeLevel();
}
