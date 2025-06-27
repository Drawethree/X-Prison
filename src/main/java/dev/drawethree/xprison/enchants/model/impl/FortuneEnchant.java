package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FortuneEnchant extends XPrisonEnchantment {

    private static List<XMaterial> blackListedBlocks;

    public FortuneEnchant(XPrisonEnchants instance) {
        super(instance, 3);
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(XMaterial::matchXMaterial).map(Optional::get).filter(Objects::nonNull).collect(Collectors.toList());
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
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return 100.0;
    }

    @Override
    public void reload() {
        super.reload();
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(XMaterial::matchXMaterial).map(Optional::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    public static boolean isBlockBlacklisted(Block block) {
        XMaterial blockMaterial = XMaterial.matchXMaterial(block.getType());
        return blackListedBlocks.contains(blockMaterial);
    }
}
