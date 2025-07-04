package dev.drawethree.xprison.utils.item;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class NBTUtils {

    public static ItemStack setTag(ItemStack item, String key, String value) {
        try {
            // Get CraftItemStack
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".inventory.CraftItemStack");
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopy.invoke(null, item);

            // Get or create NBTTagCompound
            Class<?> itemStackClass = nmsItem.getClass();
            Method getTag = itemStackClass.getMethod("getTag");
            Method setTag = itemStackClass.getMethod("setTag", Class.forName("net.minecraft.server." + getVersion() + ".NBTTagCompound"));

            Object tag = getTag.invoke(nmsItem);
            if (tag == null) {
                tag = Class.forName("net.minecraft.server." + getVersion() + ".NBTTagCompound").newInstance();
            }

            Method setString = tag.getClass().getMethod("setString", String.class, String.class);
            setString.invoke(tag, key, value);
            setTag.invoke(nmsItem, tag);

            // Convert back to Bukkit ItemStack
            Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItem.getClass());
            return (ItemStack) asBukkitCopy.invoke(null, nmsItem);

        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public static String getTag(ItemStack item, String key) {
        try {
            // Get CraftItemStack
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".inventory.CraftItemStack");
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopy.invoke(null, item);

            // Get NBT tag
            Class<?> itemStackClass = nmsItem.getClass();
            Method getTag = itemStackClass.getMethod("getTag");
            Object tag = getTag.invoke(nmsItem);
            if (tag == null) return null;

            Method getString = tag.getClass().getMethod("getString", String.class);
            return (String) getString.invoke(tag, key);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
