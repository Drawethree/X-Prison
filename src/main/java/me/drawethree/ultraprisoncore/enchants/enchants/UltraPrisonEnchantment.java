package me.drawethree.ultraprisoncore.enchants.enchants;

import lombok.Getter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.*;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.text.Text;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

@Getter
public abstract class UltraPrisonEnchantment implements Refundable {

    private static HashMap<Integer, UltraPrisonEnchantment> allEnchantmentsById;
    private static HashMap<String, UltraPrisonEnchantment> allEnchantmentsByName;

    static {
        loadEnchantments();
    }

    protected final UltraPrisonEnchants plugin;

    protected final int id;
    private String rawName;
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
        this.rawName = this.plugin.getConfig().get().getString("enchants." + id + ".RawName");
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
        return allEnchantmentsById.values();
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
        return allEnchantmentsById.get(id);
    }

    public static UltraPrisonEnchantment getEnchantByName(String name) {
        return allEnchantmentsByName.get(name.toLowerCase());
    }

    public static void loadEnchantments() {
        allEnchantmentsById = new HashMap<>();
        allEnchantmentsByName = new HashMap<>();

        registerEnchant(new EfficiencyEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new UnbreakingEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new FortuneEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new HasteEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new SpeedEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new JumpBoostEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new NightVisionEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new LuckyBoosterEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new ExplosiveEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new LayerEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new CharityEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new SalaryEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new BlessingEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new TokenatorEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new KeyFinderEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new PrestigeFinderEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new BlockBoosterEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new FuelEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new AutoSellEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new AutoSellEnchant(UltraPrisonEnchants.getInstance()));
        registerEnchant(new VoucherFinderEnchant(UltraPrisonEnchants.getInstance()));

        registerCustomEnchants();
    }

    private static void registerCustomEnchants() {
        //TODO: Find all extra classes and register them.
    }


    public int getMaxLevel() {
        return this.maxLevel == -1 ? Integer.MAX_VALUE : this.maxLevel;
    }


    public static boolean registerEnchant(UltraPrisonEnchantment enchantment) {

        Validate.notNull(enchantment.getRawName());

        allEnchantmentsById.put(enchantment.getId(), enchantment);
        allEnchantmentsByName.put(enchantment.getRawName().toLowerCase(), enchantment);
        return true;
    }
}
