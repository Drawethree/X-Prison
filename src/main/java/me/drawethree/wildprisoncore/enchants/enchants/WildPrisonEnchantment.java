package me.drawethree.wildprisoncore.enchants.enchants;

import lombok.Getter;
import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.implementations.*;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

@Getter
public abstract class WildPrisonEnchantment implements Refundable {

    private static  HashMap<Integer, WildPrisonEnchantment> allEnchantments;

    static {
        loadEnchantments();
    }

    protected final WildPrisonEnchants plugin;

    protected final int id;
    private String name;
    private Material material;
    private String description;
    private boolean enabled;
    private int guiSlot;
    private int maxLevel;
    private long cost;
    private long increaseCost;

    public WildPrisonEnchantment(WildPrisonEnchants plugin, int id) {
        this.plugin = plugin;
        this.id = id;
        this.name = Text.colorize(this.plugin.getConfig().get().getString("enchants." + id + ".Name"));
        this.material = Material.valueOf(this.plugin.getConfig().get().getString("enchants." + id + ".Material"));
        this.description = Text.colorize(this.plugin.getConfig().get().getString("enchants." + id + ".Description"));
        this.enabled = this.plugin.getConfig().get().getBoolean("enchants." + id + ".Enabled");
        this.guiSlot = this.plugin.getConfig().get().getInt("enchants." + id + ".InGuiSlot");
        this.maxLevel = this.plugin.getConfig().get().getInt("enchants." + id + ".Max");
        this.cost = this.plugin.getConfig().get().getLong("enchants." + id + ".Cost");
        this.increaseCost = this.plugin.getConfig().get().getLong("enchants." + id + ".Increase-Cost-by");
    }

    public abstract void onEquip(Player p, ItemStack pickAxe, int level);

    public abstract void onUnequip(Player p, ItemStack pickAxe, int level);
    public abstract void onBlockBreak(BlockBreakEvent e, int enchantLevel);

    public static Collection<WildPrisonEnchantment> all() {
        return allEnchantments.values();
    }

    public long getCostOfLevel(int level) {
        return (this.cost + (this.increaseCost * (level - 1)));
    }


    @Override
    public boolean isRefundEnabled() {
        return this.plugin.getConfig().get().getBoolean("enchants." + this.id + ".Refund.Enabled");
    }

    @Override
    public int refundGuiSlot() {
        return this.plugin.getConfig().get().getInt("enchants." + this.id + ".Refund.InGuiSlot");
    }

    public static WildPrisonEnchantment getEnchantById(int id) {
        return allEnchantments.get(id);
    }

    public static void loadEnchantments() {
        allEnchantments = new HashMap<>();
        allEnchantments.put(1, new EfficiencyEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(2, new UnbreakingEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(3, new FortuneEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(4, new HasteEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(5, new SpeedEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(6, new JumpBoostEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(7, new NightVisionEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(8, new LuckyBoosterEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(9, new ExplosiveEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(10, new JackHammerEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(11, new CharityEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(12, new SalaryEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(13, new BlessingEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(14, new TokenatorEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(15, new KeyFinderEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(16, new PrestigeFinderEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(17, new BoosterEnchant(WildPrisonEnchants.getInstance()));
        allEnchantments.put(18, new FuelEnchant(WildPrisonEnchants.getInstance()));
    }
}
