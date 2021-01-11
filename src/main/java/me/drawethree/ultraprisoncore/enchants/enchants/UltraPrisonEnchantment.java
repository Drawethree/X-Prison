package me.drawethree.ultraprisoncore.enchants.enchants;

import lombok.Getter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.*;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

@Getter
public abstract class UltraPrisonEnchantment implements Refundable {

    private static
    HashMap<Integer, UltraPrisonEnchantment> allEnchantments;

    static {
        loadEnchantments();
    }

    protected final UltraPrisonEnchants plugin;

    protected final int id;
    private String name;
    private Material material;
    private String description;
    private boolean enabled;
    private int guiSlot;
    private int maxLevel;
    private long cost;
    private long increaseCost;

    public UltraPrisonEnchantment(UltraPrisonEnchants plugin, int id) {
        this.plugin = plugin;
        this.id = id;
        this.name = Text.colorize(this.plugin.getConfig().get().getString("enchants." + id + ".Name"));
		this.material = CompMaterial.fromString(this.plugin.getConfig().get().getString("enchants." + id + ".Material")).toMaterial();
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

    public static Collection<UltraPrisonEnchantment> all() {
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

    public static UltraPrisonEnchantment getEnchantById(int id) {
        return allEnchantments.get(id);
    }

    public static void loadEnchantments() {
        allEnchantments = new HashMap<>();
        allEnchantments.put(1, new EfficiencyEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(2, new UnbreakingEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(3, new FortuneEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(4, new HasteEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(5, new SpeedEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(6, new JumpBoostEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(7, new NightVisionEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(8, new LuckyBoosterEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(9, new ExplosiveEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(10, new LayerEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(11, new CharityEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(12, new SalaryEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(13, new BlessingEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(14, new TokenatorEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(15, new KeyFinderEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(16, new PrestigeFinderEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(17, new BlockBoosterEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(18, new FuelEnchant(UltraPrisonEnchants.getInstance()));
        allEnchantments.put(19, new AutoSellEnchant(UltraPrisonEnchants.getInstance()));

        registerCustomEnchants();
    }

    private static void registerCustomEnchants() {
        //TODO: Find all extra classes and register them.
    }


    public int getMaxLevel() {
        return this.maxLevel == -1 ? Integer.MAX_VALUE : this.maxLevel;
    }


    public static boolean registerEnchant(UltraPrisonEnchantment enchantment) {
        allEnchantments.put(enchantment.getId(), enchantment);
        return true;
    }
}
