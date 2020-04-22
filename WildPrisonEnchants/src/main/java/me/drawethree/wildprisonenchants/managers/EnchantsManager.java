package me.drawethree.wildprisonenchants.managers;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnchantsManager {


    private final WildPrisonEnchants plugin;

    private static List<String> PICKAXE_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("Pickaxe.lore");

    public EnchantsManager(WildPrisonEnchants plugin) {
        this.plugin = plugin;
    }


    public HashMap<WildPrisonEnchantment, Integer> getPlayerEnchants(Player p) {
        HashMap<WildPrisonEnchantment, Integer> returnMap = new HashMap<>();
        ItemStack item = findPickaxe(p);
        if (item == null) {
            return returnMap;
        } else {
            for (WildPrisonEnchantment enchantment : WildPrisonEnchantment.all()) {
                int level = this.getEnchantLevel(item, enchantment.getId());
                if (level == 0) {
                    continue;
                }
                returnMap.put(enchantment, level);

            }
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
        Schedulers.async().run(() -> {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();

            for (String s : PICKAXE_LORE) {
                s = s.replace("%Blocks%", String.valueOf(getBlocksBroken(item)));

                try {
                    int enchId = Integer.parseInt(s.replace("%Enchant-", "").replace("%", ""));
                    WildPrisonEnchantment enchantment = WildPrisonEnchantment.getEnchantById(enchId);
                    if (enchantment != null) {
                        int enchLvl = getEnchantLevel(item, enchId);
                        if(enchLvl > 0) {
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
        });
    }

    public long getBlocksBroken(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if (!tag.hasKey("blocks-broken")) {
            return 0;
        }

        return tag.getInt("blocks-broken");
    }

    public void addBlocksBroken(Player p, int amount) {
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

    public int getEnchantLevel(ItemStack itemStack, int id) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        if (!tag.hasKey(WildPrisonEnchantment.NBT_TAG_INDETIFIER + id)) {
            return 0;
        }
        return tag.getInt(WildPrisonEnchantment.NBT_TAG_INDETIFIER + id);
    }

    public int getEnchantLevel(Player p, int id) {
        ItemStack item = findPickaxe(p);
        if (item == null) {
            return 0;
        }
        return getEnchantLevel(item, id);
    }

    public void handleBlockBreak(BlockBreakEvent e) {
        Schedulers.async().run(() -> {
            HashMap<WildPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(e.getPlayer());
           for (WildPrisonEnchantment enchantment : playerEnchants.keySet()) {
               enchantment.onBlockBreak(e, playerEnchants.get(enchantment));
           }
        });
    }
}
