package me.drawethree.wildprisonenchants.managers;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisonenchants.gui.DisenchantGUI;
import me.drawethree.wildprisonenchants.gui.EnchantGUI;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnchantsManager {


    public static final String NBT_TAG_INDETIFIER = "wild-prison-ench-";

    private static final Material ENCHANT_GUI_ITEM_MATERIAL = Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("enchant_menu.item.material"));
    private static final List<String> ENCHANT_GUI_ITEM_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("enchant_menu.item.lore");

    private static final Material DISENCHANT_GUI_ITEM_MATERIAL = Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.item.material"));
    private static final List<String> DISENCHANT_GUI_ITEM_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("disenchant_menu.item.lore");


    private final WildPrisonEnchants plugin;

    private static final long OBSIDIAN_TOKENS = WildPrisonEnchants.getInstance().getConfig().getLong("obsidian_tokens");
    private static final long ENDSTONE_TOKENS = WildPrisonEnchants.getInstance().getConfig().getLong("endstone_tokens");

    private static List<String> PICKAXE_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("Pickaxe.lore");

    public EnchantsManager(WildPrisonEnchants plugin) {
        this.plugin = plugin;
    }


    public HashMap<WildPrisonEnchantment, Integer> getPlayerEnchants(ItemStack pickAxe) {
        HashMap<WildPrisonEnchantment, Integer> returnMap = new HashMap<>();
        for (WildPrisonEnchantment enchantment : WildPrisonEnchantment.all()) {
            int level = this.getEnchantLevel(pickAxe, enchantment.getId());
            if (level == 0) {
                continue;
            }
            returnMap.put(enchantment, level);
        }
        return returnMap;
    }


    public ItemStack findPickaxe(Player p) {
        for (ItemStack i : p.getInventory()) {
            if (i == null) {
                continue;
            }
            if (i.getType() == Material.DIAMOND_PICKAXE) {
                return i;
            }
        }
        return null;
    }

    public void applyLoreToPickaxe(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        for (String s : PICKAXE_LORE) {
            s = s.replace("%Blocks%", String.valueOf(getBlocksBroken(item)));

            try {
                int enchId = Integer.parseInt(s.replace("%Enchant-", "").replace("%", ""));
                WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(enchId);
                if (enchantment != null) {
                    int enchLvl = getEnchantLevel(item, enchId);
                    if (enchLvl > 0) {
                        s = enchantment.getName() + " " + enchLvl;
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {

            }
            lore.add(Text.colorize(s));
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public long getBlocksBroken(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if (!tag.hasKey("blocks-broken")) {
            return 0;
        }

        return tag.getInt("blocks-broken");
    }

    public synchronized void addBlocksBrokenToItem(Player p, int amount) {

        if (amount == 0) {
            return;
        }

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(p.getItemInHand());

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if (!tag.hasKey("blocks-broken")) {
            tag.setInt("blocks-broken", 0);
        }

        tag.setInt("blocks-broken", tag.getInt("blocks-broken") + amount);
        nmsItem.setTag(tag);

        p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        applyLoreToPickaxe(p.getItemInHand());
    }

    public boolean hasEnchant(Player p, int id) {
        ItemStack item = findPickaxe(p);
        if (item == null) {
            return false;
        }
        return getEnchantLevel(item, id) != 0;
    }

    public synchronized int getEnchantLevel(ItemStack itemStack, int id) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        if (!tag.hasKey(NBT_TAG_INDETIFIER + id)) {
            return 0;
        }
        return tag.getInt(NBT_TAG_INDETIFIER + id);
    }

    public synchronized int getEnchantLevel(Player p, int id) {
        ItemStack item = findPickaxe(p);
        if (item == null) {
            return 0;
        }
        return getEnchantLevel(item, id);
    }

    public void handleBlockBreak(BlockBreakEvent e, ItemStack pickAxe) {
        if (e.getBlock().getType() == Material.ENDER_STONE) {
            WildPrisonTokens.getApi().addTokens(e.getPlayer(), ENDSTONE_TOKENS);
        } else if (e.getBlock().getType() == Material.OBSIDIAN) {
            WildPrisonTokens.getApi().addTokens(e.getPlayer(), OBSIDIAN_TOKENS);
        }
        HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(pickAxe);
        for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
            enchantment.onBlockBreak(e, playerEnchants.get(enchantment));
        }
    }

    public void handlePickaxeEquip(Player p, ItemStack newItem) {
        HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(newItem);
        for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
            enchantment.onEquip(p, newItem, playerEnchants.get(enchantment));
        }
    }

    public void handlePickaxeUnequip(Player p, ItemStack newItem) {
        HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(newItem);
        for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
            enchantment.onUnequip(p, newItem, playerEnchants.get(enchantment));
        }
    }

    public boolean addEnchant(Player p, int id, int level) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);

        if (enchantment == null || p.getItemInHand() == null) {
            return false;
        }

        ItemStack item = p.getItemInHand();
        enchantment.onUnequip(p, item, level);
        enchantment.onEquip(p, item, level);
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if (level > 0) {
            tag.setInt(EnchantsManager.NBT_TAG_INDETIFIER + enchantment.getId(), level);
            nmsItem.setTag(tag);
        }


        p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        this.applyLoreToPickaxe(p.getItemInHand());
        return true;
    }

    public boolean addEnchant(Player p, WildPrisonEnchantment enchantment, int level) {
        return addEnchant(p, enchantment.getId(), level);
    }

    public boolean removeEnchant(Player p, int id) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);
        if (enchantment == null || p.getItemInHand() == null) {
            return false;
        }

        ItemStack item = p.getItemInHand();
        enchantment.onEquip(p, item, 0);
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        tag.remove(NBT_TAG_INDETIFIER + id);
        nmsItem.setTag(tag);

        p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        applyLoreToPickaxe(p.getItemInHand());
        return true;
    }

    public boolean removeEnchant(Player p, int id, int level) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);

        if (enchantment == null || p.getItemInHand() == null || level == 0) {
            return false;
        }

        ItemStack item = p.getItemInHand();
        enchantment.onEquip(p, item, level - 1);
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        tag.setInt(NBT_TAG_INDETIFIER + id, level - 1);
        nmsItem.setTag(tag);

        p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        applyLoreToPickaxe(item);
        return true;
    }

    public boolean buyEnchnant(WildPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel, int addition) {

        for (int i = 0; i < addition; i++, currentLevel++) {

            if (currentLevel >= enchantment.getMaxLevel()) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_max_level"));
                return false;
            }

            long cost = enchantment.getCostOfLevel(currentLevel + 1);

            if (!WildPrisonTokens.getApi().hasEnough(gui.getPlayer(), cost)) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("not_enough_tokens"));
                return false;
            }

            WildPrisonTokens.getApi().removeTokens(gui.getPlayer(), cost);

            this.addEnchant(gui.getPlayer(), enchantment.getId(), currentLevel + 1);
            gui.setPickAxe(gui.getPlayer().getItemInHand());
            gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_bought").replace("%tokens%", String.valueOf(cost)));
        }
        return true;
    }

    public boolean disenchant(WildPrisonEnchantment enchantment, DisenchantGUI gui, int currentLevel, int substraction) {

        long totalRefunded = 0;
        for (int i = 0; i < substraction; i++, currentLevel--) {

            if (currentLevel == 0) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_no_level"));
                return false;
            }

            long cost = enchantment.getCostOfLevel(currentLevel);

            WildPrisonTokens.getApi().addTokens(gui.getPlayer(), cost);

            this.removeEnchant(gui.getPlayer(), enchantment.getId(), currentLevel);
            this.applyLoreToPickaxe(gui.getPlayer().getItemInHand());
            gui.setPickAxe(gui.getPlayer().getItemInHand());
            totalRefunded += cost;
        }

        gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_refunded").replace("%amount%", String.valueOf(substraction)).replace("%enchant%", enchantment.getName()));
        gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(totalRefunded)));
        return true;
    }

    public Item getRefundGuiItem(WildPrisonEnchantment enchantment, DisenchantGUI gui, int level) {
        ItemStackBuilder builder = ItemStackBuilder.of(DISENCHANT_GUI_ITEM_MATERIAL);
        builder.name(enchantment.getName());
        builder.lore(translateLore(enchantment, DISENCHANT_GUI_ITEM_LORE, level));

        return builder.buildConsumer(handler -> {
            if (handler.getClick() == ClickType.LEFT) {
                this.disenchant(enchantment, gui, level, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.disenchant(enchantment, gui, level, 10);
                gui.redraw();
            }
        });
    }

    public Item getGuiItem(WildPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel) {

        ItemStackBuilder builder = ItemStackBuilder.of(ENCHANT_GUI_ITEM_MATERIAL);
        builder.name(enchantment.getName());
        builder.lore(translateLore(enchantment, ENCHANT_GUI_ITEM_LORE, currentLevel));

        return builder.buildConsumer(handler -> {
            if (handler.getClick() == ClickType.LEFT) {
                this.buyEnchnant(enchantment, gui, currentLevel, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.buyEnchnant(enchantment, gui, currentLevel, 10);
                gui.redraw();
            }
        });
    }

    private List<String> translateLore(WildPrisonEnchantment enchantment, List<String> guiItemLore, int currentLevel) {
        List<String> newList = new ArrayList<>();
        for (String s : guiItemLore) {
            newList.add(s.replace("%description%", enchantment.getDescription()).replace("%cost%", String.valueOf(enchantment.getCost() + (enchantment.getIncreaseCost() * currentLevel))).replace("%max_level%", String.valueOf(enchantment.getMaxLevel())).replace("%current_level%", String.valueOf(currentLevel)));
        }
        return newList;
    }
}
