package me.drawethree.wildprisonenchants.enchants;

import lombok.Getter;
import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.implementations.*;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Getter
public abstract class WildPrisonEnchantment implements Refundable {

    public static final String NBT_TAG_INDETIFIER = "wild-prison-ench-";

    private static final HashMap<Integer, WildPrisonEnchantment> allEnchantments;
    private static final Material ENCHANT_GUI_ITEM_MATERIAL = Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("enchant_menu.item.material"));
    private static final List<String> ENCHANT_GUI_ITEM_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("enchant_menu.item.lore");

    private static final Material DISENCHANT_GUI_ITEM_MATERIAL = Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.item.material"));
    private static final List<String> DISENCHANT_GUI_ITEM_LORE = WildPrisonEnchants.getInstance().getConfig().getStringList("disenchant_menu.item.lore");

    static {
        allEnchantments = new HashMap<>();
        allEnchantments.put(1, new EfficiencyEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(2, new UnbreakingEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(3, new FortuneEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(4, new HasteEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(5, new SpeedEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(6, new JumpBoostEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(7, new NightVisionEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(8, new FlyEnchantEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(9, new ExplosiveEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(10, new JackHammerEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(11, new CharityEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(12, new SalaryEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(13, new BlessingEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(14, new TokenatorEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(15, new KeyFinderEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(16, new MillionaireEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(17, new BoosterEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(18, new KeyallsEnchant(WildPrisonEnchants.getInstance()));
    }

    protected final WildPrisonEnchants plugin;
    protected final int id;
    private String name;
    private String description;
    private boolean enabled;
    private int guiSlot;
    private int maxLevel;
    private long cost;
    private long increaseCost;


    public WildPrisonEnchantment(WildPrisonEnchants plugin, int id) {
        this.plugin = plugin;
        this.id = id;
        this.name = Text.colorize(this.plugin.getConfig().getString("enchants." + id + ".Name"));
        this.description = Text.colorize(this.plugin.getConfig().getString("enchants." + id + ".Description"));
        this.enabled = this.plugin.getConfig().getBoolean("enchants." + id + ".Enabled");
        this.guiSlot = this.plugin.getConfig().getInt("enchants." + id + ".InGuiSlot");
        this.maxLevel = this.plugin.getConfig().getInt("enchants." + id + ".Max");
        this.cost = this.plugin.getConfig().getLong("enchants." + id + ".Cost");
        this.increaseCost = this.plugin.getConfig().getLong("enchants." + id + ".Increase-Cost-by");
    }

    public abstract void onEquip(Player p, ItemStack pickAxe, int level);
    public abstract void onUnequip(Player p, ItemStack pickAxe, int level);

    public abstract void onBlockBreak(BlockBreakEvent e, int enchantLevel);

    public static Collection<WildPrisonEnchantment> all() {
        return allEnchantments.values();
    }


    public ItemStack setEnchantToItem(Player p,ItemStack itemStack, int level) {
        this.onUnequip(p, itemStack,level);
        this.onEquip(p, itemStack, level);
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        if(level > 0) {
            tag.setInt(NBT_TAG_INDETIFIER + this.id, level);
            nmsItem.setTag(tag);
        }


        WildPrisonEnchants.getEnchantsManager().applyLoreToPickaxe(itemStack);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    private ItemStack removeEnchantFromItem(Player p, ItemStack itemStack, int level) {
        this.onEquip(p, itemStack, level - 1);
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        tag.setInt(NBT_TAG_INDETIFIER + this.id, level - 1);
        nmsItem.setTag(tag);

        WildPrisonEnchants.getEnchantsManager().applyLoreToPickaxe(itemStack);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public boolean buyEnchnant(EnchantGUI gui, int currentLevel, int addition) {

        for (int i = 0; i < addition; i++, currentLevel++) {

            if (currentLevel >= maxLevel) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_max_level"));
                return false;
            }

            long cost = getCostOfLevel(currentLevel + (i + 1));

            if (!WildPrisonTokens.getApi().hasEnough(gui.getPlayer(), cost)) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("not_enough_tokens"));
                return false;
            }

            WildPrisonTokens.getApi().removeTokens(gui.getPlayer(), cost);

            gui.setPickAxe(setEnchantToItem(gui.getPlayer(),gui.getPickAxe(), currentLevel + 1));
            WildPrisonEnchants.getEnchantsManager().applyLoreToPickaxe(gui.getPickAxe());
            gui.getPlayer().setItemInHand(gui.getPickAxe());

            gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_bought").replace("%tokens%", String.valueOf(cost)));
        }
        return true;

    }

    public boolean disenchant(DisenchantGUI gui, int currentLevel, int substraction) {

        long totalRefunded = 0;
        for (int i = 0; i < substraction; i++, currentLevel--) {

            if (currentLevel == 0) {
                gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_no_level"));
                return false;
            }

            long cost = getCostOfLevel(currentLevel);

            WildPrisonTokens.getApi().addTokens(gui.getPlayer(), cost);

            gui.setPickAxe(removeEnchantFromItem(gui.getPlayer(), gui.getPickAxe(), currentLevel));
            WildPrisonEnchants.getEnchantsManager().applyLoreToPickaxe(gui.getPickAxe());
            gui.getPlayer().setItemInHand(gui.getPickAxe());
            totalRefunded += cost;
        }

        gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_refunded").replace("%amount%", String.valueOf(substraction)).replace("%enchant%", this.name));
        gui.getPlayer().sendMessage(WildPrisonEnchants.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(totalRefunded)));
        return true;
    }


    public long getCostOfLevel(int level) {
        return (long) (this.cost + (this.increaseCost * (level - 1)));
    }


    private List<String> translateLore(List<String> guiItemLore, int currentLevel) {
        List<String> newList = new ArrayList<>();
        for (String s : guiItemLore) {
            newList.add(s.replace("%description%", this.description).replace("%cost%", String.valueOf(this.cost + (this.increaseCost * currentLevel))).replace("%max_level%", String.valueOf(this.maxLevel)).replace("%current_level%", String.valueOf(currentLevel)));
        }
        return newList;
    }


    @Override
    public boolean isRefundEnabled() {
        return this.plugin.getConfig().getBoolean("enchants." + this.id + ".Refund.Enabled");
    }

    @Override
    public int refundGuiSlot() {
        return this.plugin.getConfig().getInt("enchants." + this.id + ".Refund.InGuiSlot");
    }

    public static WildPrisonEnchantment getEnchantById(int id) {
        return allEnchantments.get(id);
    }

    public Item getRefundGuiItem(DisenchantGUI gui, int level) {
        ItemStackBuilder builder = ItemStackBuilder.of(DISENCHANT_GUI_ITEM_MATERIAL);
        builder.name(this.name);
        builder.lore(translateLore(DISENCHANT_GUI_ITEM_LORE, level));

        return builder.buildConsumer(handler -> {
            if (handler.getClick() == ClickType.LEFT) {
                this.disenchant(gui, level, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.disenchant(gui, level, 10);
                gui.redraw();
            }
        });
    }

    public Item getGuiItem(EnchantGUI gui, int currentLevel) {

        ItemStackBuilder builder = ItemStackBuilder.of(ENCHANT_GUI_ITEM_MATERIAL);
        builder.name(this.name);
        builder.lore(translateLore(ENCHANT_GUI_ITEM_LORE, currentLevel));

        return builder.buildConsumer(handler -> {
            if (handler.getClick() == ClickType.LEFT) {
                this.buyEnchnant(gui, currentLevel, 1);
                gui.redraw();
            } else if (handler.getClick() == ClickType.RIGHT) {
                this.buyEnchnant(gui, currentLevel, 10);
                gui.redraw();
            }
        });
    }
}
