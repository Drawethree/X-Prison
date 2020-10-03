package me.drawethree.wildprisoncore.enchants.managers;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.wildprisoncore.enchants.gui.EnchantGUI;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
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

    private final WildPrisonEnchants plugin;
    private List<String> ENCHANT_GUI_ITEM_LORE;
    private List<String> DISENCHANT_GUI_ITEM_LORE;
    private List<String> PICKAXE_LORE;
    private long OBSIDIAN_TOKENS;
    private long ENDSTONE_TOKENS;


    public EnchantsManager(WildPrisonEnchants plugin) {
        this.plugin = plugin;
        this.ENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("enchant_menu.item.lore");
        this.DISENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("disenchant_menu.item.lore");
        this.OBSIDIAN_TOKENS = plugin.getConfig().get().getLong("obsidian_tokens");
        this.ENDSTONE_TOKENS = plugin.getConfig().get().getLong("endstone_tokens");
        this.PICKAXE_LORE = plugin.getConfig().get().getStringList("Pickaxe.lore");
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

    private void applyLoreToPickaxe(ItemStack item) {
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
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
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

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(p.getItemInHand());

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
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        if (!tag.hasKey(NBT_TAG_INDETIFIER + id)) {
            return 0;
        }
        return tag.getInt(NBT_TAG_INDETIFIER + id);
    }

    public void handleBlockBreak(BlockBreakEvent e, ItemStack pickAxe) {
        if (e.getBlock().getType() == Material.ENDER_STONE) {
            plugin.getCore().getTokens().getApi().addTokens(e.getPlayer(), ENDSTONE_TOKENS);
        } else if (e.getBlock().getType() == Material.OBSIDIAN) {
            plugin.getCore().getTokens().getApi().addTokens(e.getPlayer(), OBSIDIAN_TOKENS);
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
        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
        HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(newItem);
        for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
            enchantment.onUnequip(p, newItem, playerEnchants.get(enchantment));
        }
    }

    public ItemStack addEnchant(Player p, ItemStack item, int id, int level) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);

        if (enchantment == null || item == null) {
            return item;
        }

        if (!p.getWorld().getName().equalsIgnoreCase("pvp")) {
            enchantment.onUnequip(p, item, level);
            enchantment.onEquip(p, item, level);
        }

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if (level > 0) {
            tag.setInt(EnchantsManager.NBT_TAG_INDETIFIER + enchantment.getId(), level);
            nmsItem.setTag(tag);
        }


        item = CraftItemStack.asBukkitCopy(nmsItem);
        this.applyLoreToPickaxe(item);
        return item;
        //p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        //this.applyLoreToPickaxe(p.getItemInHand());
        //return true;
    }

    public ItemStack addEnchant(Player p, ItemStack item, WildPrisonEnchantment enchantment, int level) {
        return addEnchant(p, item, enchantment.getId(), level);
    }

    public boolean removeEnchant(Player p, int id) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);
        if (enchantment == null || p.getItemInHand() == null) {
            return false;
        }

        ItemStack item = p.getItemInHand();
        if (!p.getWorld().getName().equalsIgnoreCase("pvp")) {
            enchantment.onEquip(p, item, 0);
        }
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        tag.remove(NBT_TAG_INDETIFIER + id);
        nmsItem.setTag(tag);

        p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        applyLoreToPickaxe(p.getItemInHand());
        return true;
    }

    public ItemStack removeEnchant(ItemStack item, Player p, int id, int level) {
        WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(id);

        if (enchantment == null || item == null || level == 0) {
            return item;
        }

        if (!p.getWorld().getName().equalsIgnoreCase("pvp")) {
            enchantment.onEquip(p, item, level - 1);
        }

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        tag.setInt(NBT_TAG_INDETIFIER + id, level - 1);
        nmsItem.setTag(tag);


        item = CraftItemStack.asBukkitCopy(nmsItem);
        this.applyLoreToPickaxe(item);
        return item;
        //p.setItemInHand(CraftItemStack.asBukkitCopy(nmsItem));
        //applyLoreToPickaxe(item);
        //return true;
    }

    public boolean buyEnchnant(WildPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel, int addition) {

        for (int i = 0; i < addition; i++, currentLevel++) {

            if (currentLevel >= enchantment.getMaxLevel()) {
                gui.getPlayer().sendMessage(plugin.getMessage("enchant_max_level"));
                return false;
            }

            long cost = enchantment.getCostOfLevel(currentLevel + 1);

            if (!plugin.getCore().getTokens().getApi().hasEnough(gui.getPlayer(), cost)) {
                gui.getPlayer().sendMessage(plugin.getMessage("not_enough_tokens"));
                return false;
            }

            plugin.getCore().getTokens().getApi().removeTokens(gui.getPlayer(), cost);

            ItemStack item = this.addEnchant(gui.getPlayer(), gui.getPickAxe(), enchantment.getId(), currentLevel + 1);
            gui.setPickAxe(item);
            gui.getPlayer().sendMessage(plugin.getMessage("enchant_bought").replace("%tokens%", String.valueOf(cost)));
        }
        return true;
    }

    public boolean disenchant(WildPrisonEnchantment enchantment, DisenchantGUI gui, int currentLevel, int substraction) {


        if (currentLevel <= 0) {
            gui.getPlayer().sendMessage(plugin.getMessage("enchant_no_level"));
            return false;
        }

        long totalRefunded = 0;

        for (int i = 0; i < substraction; i++, currentLevel--) {

            if (currentLevel <= 0) {
                break;
            }

            long cost = enchantment.getCostOfLevel(currentLevel);

            plugin.getCore().getTokens().getApi().addTokens(gui.getPlayer(), cost);

            ItemStack item = this.removeEnchant(gui.getPickAxe(), gui.getPlayer(), enchantment.getId(), currentLevel);
            gui.setPickAxe(item);
            totalRefunded += cost;
        }

        gui.getPlayer().sendMessage(plugin.getMessage("enchant_refunded").replace("%amount%", String.valueOf(substraction)).replace("%enchant%", enchantment.getName()));
        gui.getPlayer().sendMessage(plugin.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(totalRefunded)));
        return true;
    }

    public Item getRefundGuiItem(WildPrisonEnchantment enchantment, DisenchantGUI gui, int level) {
        Material m = enchantment.isRefundEnabled() ? enchantment.getMaterial() : Material.BARRIER;
        ItemStackBuilder builder = ItemStackBuilder.of(m);
        builder.name(enchantment.isRefundEnabled() ? enchantment.getName() : this.plugin.getMessage("enchant_cant_disenchant"));
        builder.lore(enchantment.isRefundEnabled() ? translateLore(enchantment, DISENCHANT_GUI_ITEM_LORE, level) : new ArrayList<>());

        return enchantment.isRefundEnabled() ? builder.buildItem().bind(handler -> {
            if (handler.isShiftClick()) {
                this.disenchant(enchantment, gui, level, 100);
                gui.redraw();
            } else if (handler.getClick() == ClickType.LEFT) {
                this.disenchant(enchantment, gui, level, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.disenchant(enchantment, gui, level, 10);
                gui.redraw();
            }
        }, ClickType.SHIFT_RIGHT, ClickType.LEFT, ClickType.RIGHT).build() : builder.buildConsumer(handler -> handler.getWhoClicked().sendMessage(this.plugin.getMessage("enchant_cant_disenchant")));
    }

    public Item getGuiItem(WildPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel) {

        ItemStackBuilder builder = ItemStackBuilder.of(enchantment.getMaterial());
        builder.name(enchantment.getName());
        builder.lore(translateLore(enchantment, ENCHANT_GUI_ITEM_LORE, currentLevel));

        return builder.buildItem().bind(handler -> {
            if (handler.getClick() == ClickType.SHIFT_RIGHT) {
                this.buyEnchnant(enchantment, gui, currentLevel, 100);
                gui.redraw();
            } else if (handler.getClick() == ClickType.LEFT) {
                this.buyEnchnant(enchantment, gui, currentLevel, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.buyEnchnant(enchantment, gui, currentLevel, 10);
                gui.redraw();
            }
        }, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT).build();
    }

    private List<String> translateLore(WildPrisonEnchantment enchantment, List<String> guiItemLore, int currentLevel) {
        List<String> newList = new ArrayList<>();
        for (String s : guiItemLore) {
            newList.add(s.replace("%description%", enchantment.getDescription()).replace("%cost%", String.format("%,d", enchantment.getCost() + (enchantment.getIncreaseCost() * currentLevel))).replace("%max_level%", String.format("%,d", enchantment.getMaxLevel())).replace("%current_level%", String.format("%,d", currentLevel)));
        }
        return newList;
    }

    public long getPickaxeValue(ItemStack pickAxe) {
        long sum = 0;
        HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(pickAxe);
        for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
            for (int i = 1; i <= playerEnchants.get(enchantment); i++) {
                sum += enchantment.getCostOfLevel(i);
            }
        }
        return sum;
    }

	public void reloadConfig() {
		this.ENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("enchant_menu.item.lore");
		this.DISENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("disenchant_menu.item.lore");
		this.OBSIDIAN_TOKENS = plugin.getConfig().get().getLong("obsidian_tokens");
		this.ENDSTONE_TOKENS = plugin.getConfig().get().getLong("endstone_tokens");
		this.PICKAXE_LORE = plugin.getConfig().get().getStringList("Pickaxe.lore");
	}
}
