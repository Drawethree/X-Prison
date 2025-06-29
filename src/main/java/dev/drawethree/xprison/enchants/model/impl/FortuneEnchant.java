package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FortuneEnchant extends XPrisonEnchantmentBaseCore implements EquipabbleEnchantment {

    private static List<XMaterial> blackListedBlocks;

    public FortuneEnchant() {
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {
        ItemMeta meta = pickAxe.getItemMeta();
        meta.removeEnchant(XEnchantment.FORTUNE.get());
        pickAxe.setItemMeta(meta);
    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        List<String> blacklist = new Gson().fromJson(
                config.get("blacklist"),
                new TypeToken<List<String>>(){}.getType()
        );
        blackListedBlocks = blacklist.stream().map(XMaterial::matchXMaterial).map(Optional::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static boolean isBlockBlacklisted(Block block) {
        XMaterial blockMaterial = XMaterial.matchXMaterial(block.getType());
        return blackListedBlocks.contains(blockMaterial);
    }
}
