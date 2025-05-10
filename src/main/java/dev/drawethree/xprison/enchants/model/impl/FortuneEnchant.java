package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class FortuneEnchant extends XPrisonEnchantment {

    private final double chance;
    private static List<CompMaterial> blackListedBlocks;

    public FortuneEnchant(XPrisonEnchants instance) {
        super(instance, 3);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(CompMaterial::fromString).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void onEquip(Player p, @NotNull ItemStack pickAxe, int level) {
        ItemMeta meta = pickAxe.getItemMeta();
        meta.addEnchant(Enchantment.FORTUNE, level, true);
        pickAxe.setItemMeta(meta);
    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(@NotNull BlockBreakEvent e, int enchantLevel) {

        double chance = getChanceToTrigger(enchantLevel);

        if (chance < ThreadLocalRandom.current().nextDouble(100)) {
            plugin.getCore().debug("Fortune enchantment chance failed: " + chance, plugin);
            return;
        }

        Block block = e.getBlock();
        Material dropType = block.getType();

        if (block.getType().name().contains("ORE")) {
            plugin.getCore().debug("Fortune enchantment drop found: " + dropType, plugin);
            return;
        }

        if (!dropType.isItem()){
            plugin.getCore().debug("Fortune enchantment failed: Block is not an item", plugin);
            return;
        }

        block.setType(Material.AIR); // Rompe el bloque

        int baseAmount = 1;
        int bonus = getBonusMultiplier(enchantLevel, super.getMaxLevel()); // Random según nivel

        ItemStack drop = new ItemStack(dropType, baseAmount + bonus);
        //block.getWorld().dropItemNaturally(block.getLocation(), drop); Los tira al inventario pero mejor que los de directamente
        e.getPlayer().getInventory().addItem(drop);
    }

    private int getBonusMultiplier(int level, int maxLevel) {
        if (level >= maxLevel / 2) { // Mitad o más
            return 4 + (int)(Math.random() * 2); // 4, 5 o 6
        } else {
            return 2 + (int)(Math.random() * 1); // 2 o 3
        }
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void reload() {
        super.reload();
        blackListedBlocks = plugin.getEnchantsConfig().getYamlConfig().getStringList("enchants." + id + ".Blacklist").stream().map(CompMaterial::fromString).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    public static boolean isBlockBlacklisted(Block block) {
        CompMaterial blockMaterial = CompMaterial.fromBlock(block);
        return blackListedBlocks.contains(blockMaterial);
    }
}
