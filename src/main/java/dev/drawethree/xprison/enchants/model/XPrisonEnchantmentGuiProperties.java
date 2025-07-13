package dev.drawethree.xprison.enchants.model;

import org.bukkit.Material;

import java.util.Collection;

public interface XPrisonEnchantmentGuiProperties {

    /**
     * Gets the slot number in the enchanting GUI where this enchantment is displayed.
     *
     * @return The GUI slot index.
     */
    int getGuiSlot();

    /**
     * Gets the material type of the GUI item representing this enchantment.
     *
     * @return The material used in the GUI.
     */
    Material getGuiMaterial();

    /**
     * Gets the display name of the GUI item representing this enchantment.
     * May contain color codes.
     *
     * @return The GUI item name.
     */
    String getGuiName();

    /**
     * Gets the lore (description) of the GUI item for this enchantment.
     * May contain color codes.
     *
     * @return A collection of lore strings.
     */
    Collection<String> getGuiDescription();

    /**
     * Gets the Base64 texture data for the GUI item, useful if using a custom player head.
     *
     * @return The Base64 string for the GUI item texture, or null/empty if not applicable.
     */
    String getGuiBase64();

    /**
     * Gets the custom model data for the GUI item
     *
     * @return Custom Model Data of GUI item
     */
    int getCustomModelData();
}