package dev.drawethree.xprison.enchants.model;

import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantmentGuiProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class XPrisonEnchantmentGuiPropertiesImpl implements XPrisonEnchantmentGuiProperties {

    private int guiSlot;
    private String guiName;
    private String guiBase64;
    private Material guiMaterial;
    private List<String> guiDescription;
    private int customModelData;

}