package dev.drawethree.xprison.enchants.model;

/**
 * Represents a custom enchantment in the XPrison system.
 * Each enchantment has a unique ID, name, configuration, and purchasing behavior.
 */
public interface XPrisonEnchantment {

    /**
     * Gets the unique ID of this enchantment.
     *
     * @return The unique enchantment ID.
     */
    int getId();

    /**
     * Gets the raw name of the enchantment (without color codes).
     *
     * @return The raw enchantment name.
     */
    String getRawName();

    /**
     * Gets the display name of the enchantment, including color codes.
     *
     * @return The colored enchantment name.
     */
    String getName();

    /**
     * Gets the display name of the enchantment without color codes.
     *
     * @return The enchantment name without color codes.
     */
    String getNameWithoutColor();

    /**
     * Gets the GUI properties of this enchantment, such as material, slot, and description.
     *
     * @return The GUI properties of the enchantment.
     */
    XPrisonEnchantmentGuiProperties getGuiProperties();

    /**
     * Gets the name of the developer or plugin author who created this enchantment.
     *
     * @return The author name.
     */
    String getAuthor();

    /**
     * Checks whether this enchantment is currently enabled in the system.
     *
     * @return True if the enchantment is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Gets the maximum level this enchantment can be upgraded to.
     *
     * @return The maximum allowed enchantment level.
     */
    int getMaxLevel();

    /**
     * Gets the base cost of the enchantment, used as the starting cost for level 1.
     *
     * @return The base cost of applying the enchantment.
     */
    long getBaseCost();

    /**
     * Gets the amount by which the cost increases per level.
     *
     * @return The incremental cost per enchantment level.
     */
    long getIncreaseCost();

    /**
     * Initializes or loads this enchantment. Called during plugin load or reload.
     */
    void load();

    /**
     * Cleans up or unloads this enchantment. Called during plugin shutdown or reload.
     */
    void unload();

    /**
     * Gets the type of currency used to purchase this enchantment.
     *
     * @return The currency type (e.g., TOKENS, GEMS, VAULT).
     */
    CurrencyType getCurrencyType();
}
