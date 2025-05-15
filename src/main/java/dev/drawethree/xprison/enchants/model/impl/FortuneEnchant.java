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

        if (!dropType.isItem()) {
            plugin.getCore().debug("Fortune enchantment failed: Block is not an item", plugin);
            return;
        }

        //block.setType(Material.AIR);

        int baseAmount = 1;
        int bonus = getBonusMultiplier(enchantLevel);

        ItemStack drop = new ItemStack(dropType, baseAmount + bonus);
        if (e.getBlock().getDrops().add(drop)){
            plugin.getCore().debug("Fortune enchantment drop added: " + drop + ". Data: " + drop.getType().name() + " - " + drop.getAmount(), plugin);
        } else {
            plugin.getCore().debug("Fortune enchantment drop failed: " + drop + ". Data: " + drop.getType().name() + " - " + drop.getAmount(), plugin);
        }
        block.breakNaturally();
        //e.getPlayer().getInventory().addItem(drop);
    }

    private int getBonusMultiplier(int level) {
        // Suave hasta 5x como máximo en nivel 100
        double multiplier = 1 + (Math.log(level + 1) / Math.log(100 + 1)) * 4; // Escala logarítmica
        return (int) Math.floor(multiplier) - 1; // Bonus extra sobre base 1
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        // Empieza bajo, sube suave hasta 75% en nivel 100
        return Math.min(75, enchantLevel * 0.75); // Ej: nivel 100 → 75% chance
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
