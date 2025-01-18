package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FortuneEnchant extends XPrisonEnchantment {

    private static List<CompMaterial> blackListedBlocks;

    public FortuneEnchant(XPrisonEnchants instance) {
        super(instance, 3);
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(CompMaterial::fromString).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void onEquip(Player p, @NotNull ItemStack pickAxe, int level) {
        ItemMeta meta = pickAxe.getItemMeta();
        if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_3_AND_BELOW)) {
            meta.removeEnchant(Enchantment.FORTUNE);
        } else {
            meta.removeEnchant(Enchantment.getByName("LOOT_BONUS_BLOCKS"));
        }
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
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(CompMaterial::fromString).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    public static boolean isBlockBlacklisted(Block block) {
        CompMaterial blockMaterial = CompMaterial.fromBlock(block);
        return blackListedBlocks.contains(blockMaterial);
    }
}
